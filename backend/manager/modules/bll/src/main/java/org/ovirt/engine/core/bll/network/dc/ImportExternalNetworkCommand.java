package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
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
import org.ovirt.engine.core.common.action.ImportExternalNetworkParameters;
import org.ovirt.engine.core.common.action.InternalImportExternalNetworkParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@NonTransactiveCommandAttribute
public class ImportExternalNetworkCommand<P extends ImportExternalNetworkParameters> extends CommandBase<P> {

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private NetworkLocking networkLocking;

    @Inject
    private NetworkHelper networkHelper;

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
            network = proxy.get(networkId);
        }
        return network;
    }

    private String getNetworkName() {
        return (getNetwork() == null) ? "" : getNetwork().getName();
    }

    @Override
    protected boolean validate() {
        NetworkProviderValidator providerValidator = new NetworkProviderValidator(getProvider());
        NetworkValidator networkValidator = new NetworkValidator(getNetwork());

        return validate(providerValidator.providerIsSet())
                && validate(providerValidator.providerTypeIsNetwork())
                && validate(providerValidator.validateAuthentication())
                && validate(networkValidator.networkIsSet(getParameters().getNetworkExternalId()));
    }

    @Override
    protected void executeCommand() {
        networkHelper.mapPhysicalNetworkIdIfApplicable(network.getProvidedBy(), getParameters().getDataCenterId());
        InternalImportExternalNetworkParameters parameters = new InternalImportExternalNetworkParameters(
                getProvider().getName(), getNetwork(), getParameters().getDataCenterId(),
                getParameters().isPublicUse(), getParameters().isAttachToAllClusters());

        ActionReturnValue returnValue = runInternalAction(ActionType.InternalImportExternalNetwork, parameters,
                getContext().clone().withoutLock());

        if (!returnValue.getSucceeded()) {
            propagateFailure(runInternalAction(ActionType.InternalImportExternalNetwork, parameters));
            return;
        }

        getReturnValue().setActionReturnValue(returnValue.getActionReturnValue());
        setSucceeded(true);
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
        return lockProperties.withScope(LockProperties.Scope.Execution).withWaitForever();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (isInternalExecution()) {
            return null;
        }
        return networkLocking.getNetworkProviderLock(getProviderId());
    }

}
