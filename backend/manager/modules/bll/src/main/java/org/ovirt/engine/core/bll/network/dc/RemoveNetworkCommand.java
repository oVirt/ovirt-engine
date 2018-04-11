package org.ovirt.engine.core.bll.network.dc;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveNetworkCommand<T extends RemoveNetworkParameters> extends NetworkCommon<T> {

    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private NetworkLocking networkLocking;

    private Network network;
    private Provider<?> provider;

    public RemoveNetworkCommand(Guid id) {
        super(id);
    }

    public RemoveNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected Network getNetwork() {
        if (network == null) {
            network = networkDao.get(getNetworkId());
        }

        return network;
    }

    private Guid getNetworkId() {
        return getParameters().getId();
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getProviderId());
        }

        return provider;
    }

    private Guid getProviderId() {
        return (getNetwork()==null || !getNetwork().isExternal()) ?
                null : getNetwork().getProvidedBy().getProviderId();
    }

    @Override
    protected void executeCommand() {
        setStoragePoolId(getNetwork().getDataCenterId());

        TransactionSupport.executeInNewTransaction(() -> {
            removeVnicProfiles();
            removeFromClusters();
            networkHelper.setVdsmNamesInVdsInterfaces(getNetwork());
            getCompensationContext().snapshotEntity(getNetwork());
            networkDao.remove(getNetwork().getId());
            getCompensationContext().stateChanged();
            return null;
        });

        if (getNetwork().isExternal()) {
            if (getParameters().isRemoveFromNetworkProvider()) {
                removeExternalNetwork();
            }
        }

        if (NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getNetwork())) {
            removeNetworkFromHosts();
        }

        setSucceeded(true);
    }

    private void removeFromClusters() {
        for (NetworkCluster networkCluster : networkClusterDao.getAllForNetwork(getNetwork().getId())) {
            networkClusterHelper.removeNetworkAndReassignRoles(networkCluster);
        }
    }

    private void removeNetworkFromHosts() {
        networkHelper.removeNetworkFromHostsInDataCenter(getNetwork(),
                getStoragePoolId(),
                cloneContextAndDetachFromParent()
        );
    }

    private void removeExternalNetwork() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        proxy.remove(getNetwork().getProvidedBy().getExternalId());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        NetworkValidator validator = new NetworkValidator(getNetwork());

        return validate(validator.networkIsSet(getNetworkId()))
                && validate(validator.notRemovingManagementNetwork())
                && validate(validator.notIscsiBondNetwork())
                && validate(validator.networkNotUsedByVms())
                && validate(validator.networkNotUsedByTemplates())
                && validate(getRemoveExternalNetworkValidationResult())
                && validate(validator.notLinkedToExternalNetwork());
    }

    private ValidationResult getRemoveExternalNetworkValidationResult() {
        ProviderNetwork providerNetwork = getNetwork().getProvidedBy();
        if (providerNetwork == null || !getParameters().isRemoveFromNetworkProvider()){
            return ValidationResult.VALID;
        }
        ProviderValidator providerValidator = new ProviderValidator(getProvider());
        return providerValidator.validateReadOnlyActions();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NETWORK : AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution).withWait(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getNetwork().isExternal() && !isInternalExecution()) {
            return networkLocking.getNetworkProviderLock(getProviderId());
        } else {
            return null;
        }
    }
}
