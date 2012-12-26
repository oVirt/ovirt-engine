package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public class UpdateNetworkOnClusterCommand<T extends NetworkClusterParameters> extends
        VdsGroupCommandBase<T> {

    private Network network;

    public UpdateNetworkOnClusterCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private Network getNetwork() {
        if (network == null) {
            network = getNetworkDAO().get(getNetworkCluster().getnetwork_id());
        }

        return network;
    }

    private NetworkCluster getNetworkCluster() {
        return getParameters().getNetworkCluster();
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        getNetworkClusterDAO().update(getNetworkCluster());

        if (getNetworkCluster().getis_display()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(getVdsGroupId(), getNetwork().getId());
        }

        NetworkClusterHelper.setStatus(getVdsGroupId(), getNetwork());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(networkClusterAttachmentExists());
    }

    private ValidationResult networkClusterAttachmentExists() {
        return getNetworkClusterDAO().get(getNetworkCluster().getId()) == null ?
                new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER) : ValidationResult.VALID;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER
                : AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissions = super.getPermissionCheckSubjects();
        Guid networkId = getNetworkCluster() == null ? null : getNetworkCluster().getnetwork_id();
        permissions.add(new PermissionSubject(networkId, VdcObjectType.Network, ActionGroup.ASSIGN_CLUSTER_NETWORK));
        return permissions;
    }

    /**
     * Checks the user has permissions either on Network or on Cluster for this action.<br>
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

