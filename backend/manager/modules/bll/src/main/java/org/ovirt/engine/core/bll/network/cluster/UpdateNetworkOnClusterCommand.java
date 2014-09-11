package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirectorDelegator;
import org.ovirt.engine.core.utils.NetworkUtils;

public class UpdateNetworkOnClusterCommand<T extends NetworkClusterParameters> extends
        NetworkClusterCommandBase<T> {

    private Network mgmtNetwork;
    private NetworkCluster oldNetworkCluster;

    public UpdateNetworkOnClusterCommand(T parameters) {
        super(parameters);
    }

    private Network getManagementNetwork() {
        if (mgmtNetwork == null) {
            mgmtNetwork = getNetworkDAO().getByNameAndCluster(NetworkUtils.getDefaultManagementNetworkName(), getVdsGroupId());
        }

        return mgmtNetwork;
    }

    private NetworkCluster getOldNetworkCluster() {
        if (oldNetworkCluster == null) {
            oldNetworkCluster = getNetworkClusterDAO().get(getNetworkCluster().getId());
        }

        return oldNetworkCluster;
    }

    @Override
    protected Version getClusterVersion() {
        return getVdsGroupDAO().get(getNetworkCluster().getClusterId()).getCompatibilityVersion();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected void executeCommand() {
        final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                getNetworkClusterDAO(),
                getVmDAO(),
                getNetworkCluster(),
                getNetworkName(),
                AuditLogDirectorDelegator.getInstance());
        if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
            displayNetworkClusterHelper.warnOnActiveVm();
        }

        getNetworkClusterDAO().update(getNetworkCluster());

        if (getNetworkCluster().isDisplay() != getOldNetworkCluster().isDisplay()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(getVdsGroupId(),
                    getNetworkCluster().isDisplay() ? getPersistedNetwork().getId() : getManagementNetwork().getId());
        }

        if (getNetworkCluster().isMigration() != getOldNetworkCluster().isMigration()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsMigration(getVdsGroupId(),
                    getNetworkCluster().isMigration() ? getPersistedNetwork().getId() : getManagementNetwork().getId());
        }

        NetworkClusterHelper.setStatus(getVdsGroupId(), getPersistedNetwork());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return validate(networkClusterAttachmentExists()) && validateAttachment();
    }

    private ValidationResult networkClusterAttachmentExists() {
        return getOldNetworkCluster() == null ?
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
        Guid networkId = getNetworkCluster() == null ? null : getNetworkCluster().getNetworkId();
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
