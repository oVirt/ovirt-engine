package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public class AttachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        VdsGroupCommandBase<T> {

    public AttachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        if (networkExists()) {
            getNetworkClusterDAO().update(getNetworkCluster());
        } else {
            getNetworkClusterDAO().save(new NetworkCluster(getVdsGroupId(), getNetwork().getId(),
                    NetworkStatus.OPERATIONAL, false, getNetworkCluster().isRequired()));
        }
        if (getNetwork().getCluster().isDisplay()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(getVdsGroupId(), getNetwork().getId());
        }
        NetworkClusterHelper.setStatus(getVdsGroupId(), getNetwork());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && vdsGroupExists() && changesAreClusterCompatible() && logicalNetworkExists();
    }

    private boolean logicalNetworkExists() {
        if (getNetworkDAO().get(getNetworkCluster().getNetworkId()) != null) {
            return true;
        }

        addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
        return false;
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

    private boolean networkExists() {
        List<NetworkCluster> networks = getNetworkClusterDAO().getAllForCluster(getVdsGroupId());
        for (NetworkCluster networkCluster : networks) {
            if (networkCluster.getNetworkId().equals(
                    getNetworkCluster().getNetworkId())) {
                return true;
            }
        }

        return false;
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
        if (networkExists()) {
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
}
