package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveNetworkCommand<T extends RemoveNetworkParameters> extends NetworkCommon<T> {
    private Network network;

    private Provider<?> provider;

    public RemoveNetworkCommand(Guid id) {
        super(id);
    }

    public RemoveNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected Network getNetwork() {
        if (network == null) {
            network = getNetworkDAO().get(getParameters().getId());
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

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void> () {
            @Override
            public Void runInTransaction() {
                removeVnicProfiles();
                removeFromClusters();
                getCompensationContext().snapshotEntity(getNetwork());
                getNetworkDAO().remove(getNetwork().getId());
                getCompensationContext().stateChanged();
                return null;
            }
        });

        if (getNetwork().isExternal()) {
            if (getParameters().isRemoveFromNetworkProvider()) {
                removeExternalNetwork();
            }
        }

        if (NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getNetwork(), getStoragePool().getcompatibility_version())) {
            removeNetworkFromHosts();
        }

        setSucceeded(true);
    }

    private void removeFromClusters() {
        for (NetworkCluster networkCluster : getNetworkClusterDAO().getAllForNetwork(getNetwork().getId())) {
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean canDoAction() {
        NetworkValidator validator = new NetworkValidator(getNetworkDAO().get(getNetwork().getId()));
        return validate(validator.networkIsSet())
                && validate(validator.notManagementNetwork())
                && validate(validator.notIscsiBondNetwork())
                && validate(validator.networkNotUsedByVms())
                && validate(validator.networkNotUsedByTemplates());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NETWORK : AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
    }
}
