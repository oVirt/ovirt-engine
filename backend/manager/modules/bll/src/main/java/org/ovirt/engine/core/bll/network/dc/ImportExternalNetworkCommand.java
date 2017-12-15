package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.action.ImportExternalNetworkParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class ImportExternalNetworkCommand<P extends ImportExternalNetworkParameters> extends CommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private NetworkLocking networkLocking;


    private Provider<?> provider;
    private Network network;

    public ImportExternalNetworkCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getDataCenterId());
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getProviderId());
        }
        return provider;
    }

    private Guid getProviderId() {
        return getParameters().getProviderId();
    }

    private String getProviderName() {
        return (getProvider() == null) ? "" : getProvider().getName();
    }

    protected Network getNetwork() {
        if (network == null) {
            NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
            String networkId = getParameters().getNetworkExternalId();
            network = proxy.getAll().stream()
                    .filter(network -> networkId.equals(network.getProvidedBy().getExternalId()))
                    .findFirst()
                    .orElse(null);
        }
        return network;
    }

    private String getNetworkName() {
        return (getNetwork() == null) ? "" : getNetwork().getName();
    }

    @Override
    protected boolean validate() {
        NetworkProviderValidator providerValidator = new NetworkProviderValidator(getProvider());
        NetworkValidator networkValidator = new NetworkValidator(null, getNetwork());

        return validate(providerValidator.providerIsSet())
                && validate(providerValidator.providerTypeIsNetwork())
                && validate(providerValidator.validateAuthentication())
                && validate(networkValidator.networkIsSet(getParameters().getNetworkExternalId()));
    }

    @Override
    protected void executeCommand() {
        final Guid dataCenterId = getStoragePoolId();
        final Network network = getNetwork();
        network.setDataCenterId(dataCenterId);

        ActionReturnValue addNetworkReturnValue = addNetwork(dataCenterId, network);
        if (!addNetworkReturnValue.getSucceeded()) {
            propagateFailure(addNetworkReturnValue);
            return;
        }

        network.setId(addNetworkReturnValue.getActionReturnValue());

        ActionReturnValue addVnicReturnValue = addVnicProfile(network, getParameters().isPublicUse());
        if (!addVnicReturnValue.getSucceeded()) {
            propagateFailure(addVnicReturnValue);
            return;
        }

        if (getParameters().isAttachToAllClusters()) {
            ActionReturnValue attachReturnValue = attachToAllClusters(dataCenterId, network.getId());
            if (!attachReturnValue.getSucceeded()) {
                propagateFailure(attachReturnValue);
                return;
            }
        }

        getReturnValue().setActionReturnValue(network.getId());
        setSucceeded(true);
    }

    private ActionReturnValue addNetwork(Guid dataCenterId, Network network) {
        AddNetworkStoragePoolParameters params =
                new AddNetworkStoragePoolParameters(dataCenterId, network);
        params.setVnicProfileRequired(false);
        return runInternalAction(ActionType.AddNetwork, params);
    }

    private ActionReturnValue addVnicProfile(Network network, boolean publicUse) {
        VnicProfile vnicProfile = networkHelper.createVnicProfile(network);
        vnicProfile.setNetworkFilterId(null);
        AddVnicProfileParameters parameters = new AddVnicProfileParameters(vnicProfile);
        parameters.setPublicUse(publicUse);
        return runInternalAction(ActionType.AddVnicProfile, parameters);
    }

    private ActionReturnValue attachToAllClusters(Guid dataCenterId, Guid networkId) {
        QueryReturnValue queryReturnValue = runInternalQuery(QueryType.GetClustersByStoragePoolId,
                new IdQueryParameters(dataCenterId));

        List<Cluster> clusters = queryReturnValue.getReturnValue();

        return networkHelper.attachNetworkToClusters(networkId,
                clusters.stream().map(Cluster::getId).collect(Collectors.toList()));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("NetworkName", getNetworkName());
        addCustomValue("ProviderName", getProviderName());
        return getSucceeded() ? AuditLogType.NETWORK_IMPORT_EXTERNAL_NETWORK :
                AuditLogType.NETWORK_IMPORT_EXTERNAL_NETWORK_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (isInternalExecution()) {
            return null;
        }
        return networkLocking.getNetworkProviderLock(getProviderId());
    }

}
