package org.ovirt.engine.core.bll.provider.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.InternalImportExternalNetworkParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class SyncNetworkProviderCommand<P extends IdParameters> extends CommandBase<P> {

    private boolean errorOccurred = false;

    private static Logger log = LoggerFactory.getLogger(SyncNetworkProviderCommand.class);

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    @Inject
    protected NetworkHelper networkHelper;

    @Inject
    protected VmHandler vmHandler;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VnicProfileDao vnicProfileDao;

    @Inject
    private NetworkLocking networkLocking;

    private Provider<?> provider;

    public SyncNetworkProviderCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getProviderId());
        }

        return provider;
    }

    private Guid getProviderId() {
        return getParameters().getId();
    }

    private String getProviderName() {
        return (getProvider() == null) ? "" : getProvider().getName();
    }

    @Override
    protected boolean validate() {
        NetworkProviderValidator validator = new NetworkProviderValidator(getProvider());
        return validate(validator.providerIsSet())
                && validate(validator.providerTypeIsNetwork())
                && validate(validator.validateAuthentication());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return networkLocking.getNetworkProviderLock(getProviderId());
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        List<Network> providedNetworks = proxy.getAll();
        Set<String> providedNetworkIds = externalIds(providedNetworks);
        List<Network> providerNetworksInDb = networkDao.getAllForProvider(getProvider().getId());

        List<Cluster> clusters = clusterDao.getAllClustersByDefaultNetworkProviderId(getProvider().getId());
        Set<Guid> dataCenterIds = clusters.stream()
                .map(Cluster::getStoragePoolId)
                .collect(Collectors.toSet());

        for (Guid dataCenterId : dataCenterIds) {
            List<Network> providerNetworksInDataCenter = providerNetworksInDb.stream()
                    .filter(network -> dataCenterId.equals(network.getDataCenterId()))
                    .collect(Collectors.toList());

            providerNetworksInDataCenter.stream()
                    .filter(network -> !providedNetworkIds.contains(network.getProvidedBy().getExternalId()))
                    .forEach(network -> removeNetwork(network.getId()));

            Set<String> networksInDataCenterExternalIds = externalIds(providerNetworksInDataCenter);

            List<Guid> clusterInDataCenterIds = clusters.stream()
                    .filter(cluster -> dataCenterId.equals(cluster.getStoragePoolId()))
                    .map(Cluster::getId)
                    .collect(Collectors.toList());

            for (Network network : providedNetworks) {
                if (!networksInDataCenterExternalIds.contains(network.getProvidedBy().getExternalId())) {
                    ActionReturnValue importReturnValue = importNetwork(dataCenterId, network);
                    if (importReturnValue.getSucceeded()) {
                        network.setId(importReturnValue.getActionReturnValue());
                        propagateReturnValue(networkHelper.attachNetworkToClusters(network.getId(),
                                clusterInDataCenterIds));
                    }
                }
            }
        }
        setSucceeded(!errorOccurred);
    }

    private ActionReturnValue importNetwork(Guid dataCenterId, Network network) {
        InternalImportExternalNetworkParameters parameters = new InternalImportExternalNetworkParameters(
                getProvider().getName(), network, dataCenterId, true, false);

        return propagateReturnValue(runInternalAction(ActionType.InternalImportExternalNetwork, parameters,
                getInternalCommandContext()));
    }

    private Set<String> externalIds(List<Network> networks) {
        Set<String> set = new HashSet<>();
        for (Network network : networks) {
            set.add(network.getProvidedBy().getExternalId());
        }
        return set;
    }

    private void removeNetwork(Guid networkId) {
        disconnectVms(networkId);
        propagateReturnValue(runInternalAction(ActionType.RemoveNetwork,
                new RemoveNetworkParameters(networkId, false), getInternalCommandContext()));
    }

    private void disconnectVms(Guid networkId) {
        Map<Guid, VnicProfile> profiles = vnicProfileDao.getAllForNetwork(networkId)
                .stream()
                .collect(Collectors.toConcurrentMap(VnicProfile::getId, Function.identity()));

        for (VM vm : vmDao.getAllForNetwork(networkId)) {
            vmHandler.updateNetworkInterfacesFromDb(vm);
            for (VmNetworkInterface iface : vm.getInterfaces()) {
                if (profiles.get(iface.getVnicProfileId()) != null) {
                    log.warn(
                            "External network '{}' disappeared from provider '{}', disconnecting interface '{}' of VM '{}'.",
                            networkId,
                            getProvider().getName(),
                            iface.getName(),
                            vm.getName());
                    iface.setVnicProfileId(null);
                    iface.setPlugged(false);
                    propagateReturnValue(runInternalAction(ActionType.UpdateVmInterface,
                            new AddVmInterfaceParameters(vm.getId(), iface), getInternalCommandContext()));
                }
            }
        }
    }

    private CommandContext getInternalCommandContext() {
        return getContext().clone().withoutLock();
    }

    private ActionReturnValue propagateReturnValue(ActionReturnValue internalReturnValue) {
        if (!internalReturnValue.getSucceeded()) {
            propagateFailure(internalReturnValue);
            errorOccurred = true;
        }
        return internalReturnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("ProviderName", getProviderName());
        return getSucceeded() ? AuditLogType.PROVIDER_SYNCHRONIZED : AuditLogType.PROVIDER_SYNCHRONIZED_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SYNC);
        addValidationMessage(EngineMessage.VAR__TYPE__PROVIDER);
    }
}
