package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.network.AddNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AttachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        VdsGroupCommandBase<T> {

    private Network persistedNetwork;

    public AttachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getPersistedNetwork() == null ? null : getPersistedNetwork().getName();
    }

    @Override
    protected String getDescription() {
        String networkName = getNetworkName() == null ? "" : getNetworkName();
        String clusterName = getVdsGroup() == null ? "" : getVdsGroup().getName();
        return networkName + " - " + clusterName;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                updateNetworkAttachment(getVdsGroupId(), getNetworkCluster(), getNetwork());
                return null;
            }
        });

        if (!getPersistedNetwork().isExternal() && NetworkUtils.isLabeled(getPersistedNetwork())
                && NetworkHelper.setupNetworkSupported(getVdsGroup().getcompatibility_version())) {
            addNetworkToHosts();
        }

        setSucceeded(true);
    }


    private void addNetworkToHosts() {
        List<VdsNetworkInterface> nics =
                getDbFacade().getInterfaceDao().getAllInterfacesByLabelForCluster(getParameters().getVdsGroupId(),
                        getPersistedNetwork().getLabel());
        AddNetworkParametersBuilder builder = new AddNetworkParametersBuilder(getPersistedNetwork());
        ArrayList<VdcActionParametersBase> parameters = builder.buildParameters(nics);

        if (!parameters.isEmpty()) {
            NetworkParametersBuilder.updateParametersSequencing(parameters);
            getBackend().runInternalMultipleActions(VdcActionType.PersistentSetupNetworks, parameters);
        }
    }

    @Override
    protected boolean canDoAction() {
        return vdsGroupExists()
                && changesAreClusterCompatible()
                && logicalNetworkExists()
                && validateAttachment();
    }

    private boolean validateAttachment() {
        NetworkClusterValidator validator =
                new NetworkClusterValidator(getNetworkCluster(), getVdsGroup().getcompatibility_version());
        return (!NetworkUtils.isManagementNetwork(getNetwork())
                || validate(validator.managementNetworkAttachment(getNetworkName())))
                && validate(validator.migrationPropertySupported(getNetworkName()))
                && (!getPersistedNetwork().isExternal()
                || validateExternalNetwork(validator));
    }

    private boolean validateExternalNetwork(NetworkClusterValidator validator) {
        return validate(validator.externalNetworkSupported())
                && validate(validator.externalNetworkNotDisplay(getNetworkName()))
                && validate(validator.externalNetworkNotRequired(getNetworkName()));
    }

    private boolean logicalNetworkExists() {
        if (getPersistedNetwork() != null) {
            return true;
        }

        addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
        return false;
    }

    private Network getPersistedNetwork() {
        if (persistedNetwork == null) {
            persistedNetwork = getNetworkDAO().get(getNetworkCluster().getNetworkId());
        }
        return persistedNetwork;
    }

    private boolean changesAreClusterCompatible() {
        if (getParameters().getNetwork().isVmNetwork() == false) {
            if (!FeatureSupported.nonVmNetwork(getVdsGroup().getcompatibility_version())) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
                return false;
            }
        }
        return true;
    }

    private boolean vdsGroupExists() {
        if (!vdsGroupInDb()) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        return true;
    }

    private boolean vdsGroupInDb() {
        return getVdsGroup() != null;
    }

    private NetworkCluster getNetworkCluster() {
        return getParameters().getNetworkCluster();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP
                : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissions = new ArrayList<PermissionSubject>();

        Guid networkId = getNetworkCluster() == null ? null : getNetworkCluster().getNetworkId();
        // require permissions on network
        permissions.add(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));

        // require permissions on cluster the network is attached to
        if (networkExists(getVdsGroupId(), getNetworkCluster())) {
            permissions.add(new PermissionSubject(getParameters().getVdsGroupId(),
                    VdcObjectType.VdsGroups,
                    ActionGroup.CONFIGURE_CLUSTER_NETWORK));
        }
        return permissions;
    }

    /**
     * Checks the user has permissions either on Network or on Cluster for this action.
     */
    @Override
    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        List<String> messages = new ArrayList<String>();

        for (PermissionSubject permSubject : permSubjects) {
            messages.clear();
            if (checkSinglePermission(permSubject, messages)) {
                return true;
            }
        }

        getReturnValue().getCanDoActionMessages().addAll(messages);
        return false;
    }

    public static void updateNetworkAttachment(Guid clusterId, NetworkCluster networkCluster, Network network) {
        if (networkExists(clusterId, networkCluster)) {
            getNetworkClusterDao().update(networkCluster);
        } else {
            getNetworkClusterDao().save(new NetworkCluster(clusterId, network.getId(),
                    NetworkStatus.OPERATIONAL,
                    false,
                    networkCluster.isRequired(),
                    networkCluster.isMigration()));
        }

        if (network.getCluster().isDisplay()) {
            getNetworkClusterDao().setNetworkExclusivelyAsDisplay(clusterId, network.getId());
        }

        if (network.getCluster().isMigration()) {
            getNetworkClusterDao().setNetworkExclusivelyAsMigration(clusterId, network.getId());
        }

        NetworkClusterHelper.setStatus(clusterId, network);
    }

    private static boolean networkExists(Guid clusterId, NetworkCluster networkCluster) {
        List<NetworkCluster> networks = getNetworkClusterDao().getAllForCluster(clusterId);
        for (NetworkCluster nc : networks) {
            if (nc.getNetworkId().equals(networkCluster.getNetworkId())) {
                return true;
            }
        }

        return false;
    }

    private static NetworkClusterDao getNetworkClusterDao() {
        return DbFacade.getInstance().getNetworkClusterDao();
    }
}
