package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

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
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveNetworkCommand<T extends RemoveNetworkParameters> extends NetworkCommon<T> {

    @Inject
    private VmDao vmDao;

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
            network = getNetworkDao().get(getParameters().getId());
        }

        return network;
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = getDbFacade().getProviderDao().get(getNetwork().getProvidedBy().getProviderId());
        }

        return provider;
    }

    @Override
    protected void executeCommand() {
        setStoragePoolId(getNetwork().getDataCenterId());

        TransactionSupport.executeInNewTransaction(() -> {
            removeVnicProfiles();
            removeFromClusters();
            getCompensationContext().snapshotEntity(getNetwork());
            getNetworkDao().remove(getNetwork().getId());
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
        for (NetworkCluster networkCluster : getNetworkClusterDao().getAllForNetwork(getNetwork().getId())) {
            NetworkClusterHelper helper = new NetworkClusterHelper(networkCluster);
            helper.removeNetworkAndReassignRoles();
        }
    }

    private void removeNetworkFromHosts() {
        NetworkHelper.removeNetworkFromHostsInDataCenter(getNetwork(),
                getStoragePoolId(),
                cloneContextAndDetachFromParent()
        );
    }

    private void removeExternalNetwork() {
        NetworkProviderProxy proxy = ProviderProxyFactory.getInstance().create(getProvider());
        proxy.remove(getNetwork().getProvidedBy().getExternalId());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        NetworkValidator validator = new NetworkValidator(vmDao, getNetworkDao().get(getNetwork().getId()));

        return validate(validator.networkIsSet(getParameters().getId()))
                && validate(validator.notRemovingManagementNetwork())
                && validate(validator.notIscsiBondNetwork())
                && validate(validator.networkNotUsedByVms())
                && validate(validator.networkNotUsedByTemplates())
                && validate(getRemoveExternalNetworkValidationResult());
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
}
