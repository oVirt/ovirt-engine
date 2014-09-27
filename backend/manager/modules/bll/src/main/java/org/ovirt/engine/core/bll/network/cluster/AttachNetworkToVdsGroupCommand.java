package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.network.AddNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
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
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirectorDelegator;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AttachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        NetworkClusterCommandBase<T> {

    public AttachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
    }

    protected Network getNetwork() {
        return getParameters().getNetwork();
    }

    protected Version getClusterVersion() {
        return getVdsGroup().getCompatibilityVersion();
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
                attachNetwork(getVdsGroupId(), getNetworkCluster(), getNetwork());
                return null;
            }
        });

        if (!getPersistedNetwork().isExternal() && NetworkUtils.isLabeled(getPersistedNetwork())
                && NetworkHelper.setupNetworkSupported(getVdsGroup().getCompatibilityVersion())) {
            addNetworkToHosts();
        }

        setSucceeded(true);
    }

    private void addNetworkToHosts() {
        List<VdsNetworkInterface> nics =
                getDbFacade().getInterfaceDao().getAllInterfacesByLabelForCluster(getParameters().getVdsGroupId(),
                        getPersistedNetwork().getLabel());
        AddNetworkParametersBuilder builder = new AddNetworkParametersBuilder(getPersistedNetwork(), getContext());
        ArrayList<VdcActionParametersBase> parameters = builder.buildParameters(nics);

        if (!parameters.isEmpty()) {
            NetworkParametersBuilder.updateParametersSequencing(parameters);
            runInternalMultipleActions(VdcActionType.PersistentSetupNetworks, parameters);
        }
    }

    @Override
    protected boolean canDoAction() {
        return networkNotAttachedToCluster()
                && vdsGroupExists()
                && changesAreClusterCompatible()
                && logicalNetworkExists()
                && validateAttachment();
    }

    private boolean validateAttachment() {
        final AttachNetworkClusterValidator networkClusterValidator =
                new AttachNetworkClusterValidator(getNetworkCluster(), getClusterVersion());
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getVdsGroup(), getNetwork()))
                && validateAttachment(networkClusterValidator);
    }

    private boolean logicalNetworkExists() {
        if (getPersistedNetwork() != null) {
            return true;
        }

        addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
        return false;
    }

    private boolean changesAreClusterCompatible() {
        if (getParameters().getNetwork().isVmNetwork() == false) {
            if (!FeatureSupported.nonVmNetwork(getVdsGroup().getCompatibilityVersion())) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
                return false;
            }
        }
        return true;
    }

    private boolean networkNotAttachedToCluster() {
        if (networkExists()) {
            return failCanDoAction(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_CLUSTER);
        }

        return true;
    }

    private boolean networkExists() {
        return getNetworkClusterDAO().get(getNetworkCluster().getId()) != null;
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

    public static void attachNetwork(Guid clusterId, NetworkCluster networkCluster, Network network) {
        getNetworkClusterDao().save(new NetworkCluster(clusterId, network.getId(),
                NetworkStatus.OPERATIONAL,
                false,
                networkCluster.isRequired(),
                false,
                false));

        if (network.getCluster().isDisplay()) {
            final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                    getNetworkClusterDao(),
                    getVmDao(),
                    networkCluster,
                    network.getName(),
                    AuditLogDirectorDelegator.getInstance());
            if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
                displayNetworkClusterHelper.warnOnActiveVm();
            }

            getNetworkClusterDao().setNetworkExclusivelyAsDisplay(clusterId, network.getId());
        }

        if (network.getCluster().isMigration()) {
            getNetworkClusterDao().setNetworkExclusivelyAsMigration(clusterId, network.getId());
        }

        if (network.getCluster().isManagement()) {
            getNetworkClusterDao().setNetworkExclusivelyAsManagement(clusterId, network.getId());
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

    private static VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }
}
