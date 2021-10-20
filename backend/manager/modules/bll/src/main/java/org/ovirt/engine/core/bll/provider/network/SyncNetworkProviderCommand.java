package org.ovirt.engine.core.bll.provider.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
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
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.InternalImportExternalNetworkParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.base.client.OpenStackResponseException;

@NonTransactiveCommandAttribute
public class SyncNetworkProviderCommand<P extends IdParameters> extends CommandBase<P> {

    private boolean errorOccurred = false;
    private boolean internalCommandTriggered = false;
    private boolean authenticationFailed = false;

    private static Logger log = LoggerFactory.getLogger(SyncNetworkProviderCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

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
    private NetworkClusterDao networkClusterDao;

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
        logEvent(AuditLogType.PROVIDER_SYNCHRONIZATION_STARTED);
        try {
            List<Network> providedNetworks = getAllNetworks();
            Set<String> providedNetworkIds = externalIds(providedNetworks);
            List<Network> providerNetworksInDb = networkDao.getAllForProvider(getProvider().getId());

            List<Cluster> clusters = clusterDao.getAllClustersByDefaultNetworkProviderId(getProvider().getId());
            Set<Guid> dataCenterIds = clusters.stream()
                    .map(Cluster::getStoragePoolId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Guid dataCenterId : dataCenterIds) {
                Map<String, Network> providerNetworksInDataCenter = providerNetworksInDb.stream()
                        .filter(network -> dataCenterId.equals(network.getDataCenterId()))
                        .collect(Collectors.toMap(
                                network -> network.getProvidedBy().getExternalId(),
                                network -> network));

                providerNetworksInDataCenter.values().stream()
                        .filter(network -> !providedNetworkIds.contains(network.getProvidedBy().getExternalId()))
                        .forEach(network -> removeNetwork(network.getId()));

                List<Cluster> idsOfClustersInDataCenter = clusters.stream()
                        .filter(cluster -> dataCenterId.equals(cluster.getStoragePoolId()))
                        .collect(Collectors.toList());

                List<Guid> allClustersInDataCenterIds =
                        idsOfClustersInDataCenter.stream()
                                .map(Cluster::getId)
                                .collect(Collectors.toList());

                List<Guid> clustersWithOvsSwitchTypeIds = idsOfClustersInDataCenter.stream()
                        .filter(cluster -> cluster.hasRequiredSwitchType(SwitchType.OVS))
                        .map(Cluster::getId)
                        .collect(Collectors.toList());

                Map<String, Network> networkByName = networkDao.getAllForDataCenter(dataCenterId)
                        .stream()
                        .collect(Collectors.toMap(Network::getName, Function.identity()));

                for (Network network : providedNetworks) {
                    ProviderNetwork providerNetwork = network.getProvidedBy();
                    Network networkInDataCenter = providerNetworksInDataCenter.get(providerNetwork.getExternalId());
                    networkHelper.mapPhysicalNetworkIdIfApplicable(providerNetwork, networkByName);
                    List<Guid> clusterIds = network.getProvidedBy().isLinkedToPhysicalNetwork() ?
                            clustersWithOvsSwitchTypeIds :
                            allClustersInDataCenterIds;
                    if (networkInDataCenter == null) {
                        ActionReturnValue importReturnValue = importNetwork(dataCenterId, network);
                        if (importReturnValue.getSucceeded()) {
                            network.setId(importReturnValue.getActionReturnValue());
                            propagateReturnValue(networkHelper.attachNetworkToClusters(network.getId(), clusterIds));
                        }
                    } else {
                        updateNetwork(dataCenterId, network, networkInDataCenter);
                        updateNetworkClusters(clusterIds, network, networkInDataCenter);
                    }
                }
            }
            setSucceeded(!errorOccurred);
        } finally {
            logEvent(AuditLogType.PROVIDER_SYNCHRONIZATION_ENDED);
        }
    }

    private List<Network> getAllNetworks() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        try {
            return proxy.getAll();
        } catch (EngineException exception) {
            if (exception.getCause() instanceof OpenStackResponseException) {
                OpenStackResponseException cause = (OpenStackResponseException) exception.getCause();
                if (cause.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                    setAuthenticationFailed();
                    disableAutoSyncOnProvider();
                }
            }
            throw exception;
        }
    }

    private void disableAutoSyncOnProvider() {
        Provider<?> provider = getProvider();
        OpenstackNetworkProviderProperties networkProperties =
                (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
        networkProperties.setAutoSync(false);
        runInternalAction(ActionType.UpdateProvider, new ProviderParameters(provider));
    }

    private void updateNetwork(Guid dataCenterId, Network externalNetwork, Network networkInDataCenter) {
        boolean changed = false;

        if (!networkInDataCenter.getName().equals(externalNetwork.getName())) {
            networkInDataCenter.setName(externalNetwork.getName());
            changed = true;
        }

        if (networkInDataCenter.getMtu() != externalNetwork.getMtu()) {
            networkInDataCenter.setMtu(externalNetwork.getMtu());
            changed = true;
        }

        ProviderNetwork externalProviderNetwork = externalNetwork.getProvidedBy();
        ProviderNetwork dataCenterProviderNetwork = networkInDataCenter.getProvidedBy();
        if (!Objects.equals(dataCenterProviderNetwork.getPhysicalNetworkId(),
                externalProviderNetwork.getPhysicalNetworkId())) {
            dataCenterProviderNetwork.setPhysicalNetworkId(externalProviderNetwork.getPhysicalNetworkId());
            changed = true;
        }

        if (changed) {
            AddNetworkStoragePoolParameters parameters =
                    new AddNetworkStoragePoolParameters(dataCenterId, networkInDataCenter);
            propagateReturnValue(runInternalAction(ActionType.UpdateNetwork, parameters,
                    getInternalCommandContext()));
        }
    }

    private void updateNetworkClusters(List<Guid> clusterInDataCenterIds, Network externalNetwork,
            Network networkInDataCenter) {
        Set<Guid> networkClustersWithNetwork = networkClusterDao.getAllForNetwork(networkInDataCenter.getId()).stream()
                .map(NetworkCluster::getClusterId)
                .collect(Collectors.toSet());

        List<Guid> clustersToAttach = clusterInDataCenterIds.stream()
                .filter(id -> !networkClustersWithNetwork.contains(id))
                .collect(Collectors.toList());

        if (clustersToAttach.size() > 0) {
            propagateReturnValue(networkHelper.attachNetworkToClusters(networkInDataCenter.getId(), clustersToAttach));
        }
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
        setInternalCommandTriggered();
        if (!internalReturnValue.getSucceeded()) {
            propagateFailure(internalReturnValue);
            errorOccurred = true;
        }
        return internalReturnValue;
    }

    private void setInternalCommandTriggered() {
        this.internalCommandTriggered = true;
    }

    private boolean isInternalCommandTriggered() {
        return internalCommandTriggered;
    }

    private boolean isAuthenticationFailed() {
        return authenticationFailed;
    }

    private void setAuthenticationFailed() {
        this.authenticationFailed = true;
    }

    private void logEvent(AuditLogType eventType) {
        var logable = new AuditLogableImpl();
        logable.addCustomValue("ProviderName", getProviderName());
        auditLogDirector.log(logable, eventType);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("ProviderName", getProviderName());

        if (isAuthenticationFailed()) {
            return AuditLogType.PROVIDER_SYNCHRONIZED_DISABLED;
        }

        if (isInternalCommandTriggered() && getSucceeded()) {
            return AuditLogType.PROVIDER_SYNCHRONIZED_PERFORMED;
        }

        if (isInternalCommandTriggered() && !getSucceeded()) {
            return AuditLogType.PROVIDER_SYNCHRONIZED_PERFORMED_FAILED;
        }

        if (!isInternalCommandTriggered() && getSucceeded()) {
            return AuditLogType.UNASSIGNED;
        }

        return AuditLogType.PROVIDER_SYNCHRONIZED_FAILED;
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
