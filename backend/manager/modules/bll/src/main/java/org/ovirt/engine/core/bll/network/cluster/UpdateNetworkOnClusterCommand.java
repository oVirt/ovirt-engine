package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidateSupportsTransaction;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ValidateSupportsTransaction
public class UpdateNetworkOnClusterCommand<T extends NetworkClusterParameters> extends NetworkClusterCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private ManagementNetworkUtil managementNetworkUtil;
    @Inject
    private UpdateNetworkClusterPermissionsChecker permissionsChecker;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private GlusterBrickDao glusterBrickDao;

    private NetworkCluster oldNetworkCluster;

    public UpdateNetworkOnClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private NetworkCluster getOldNetworkCluster() {
        if (oldNetworkCluster == null) {
            oldNetworkCluster = networkClusterDao.get(getNetworkCluster().getId());
        }

        return oldNetworkCluster;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected void executeCommand() {
        final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                networkClusterDao,
                vmDao,
                clusterDao,
                getNetworkCluster(),
                getNetworkName(),
                auditLogDirector);
        if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
            displayNetworkClusterHelper.warnOnActiveVm();
        }

        preserveStatus();

        networkClusterDao.update(getNetworkCluster());

        final Network managementNetwork;

        if (getNetworkCluster().isManagement() && !getOldNetworkCluster().isManagement()) {
            networkClusterDao.setNetworkExclusivelyAsManagement(getClusterId(), getPersistedNetwork().getId());
            managementNetwork = getPersistedNetwork();
        } else {
            managementNetwork = managementNetworkUtil.getManagementNetwork(getClusterId());
        }

        if (getNetworkCluster().isDisplay() != getOldNetworkCluster().isDisplay()) {
            networkClusterDao.setNetworkExclusivelyAsDisplay(getClusterId(),
                    getNetworkCluster().isDisplay() ? getPersistedNetwork().getId() : managementNetwork.getId());
        }

        if (getNetworkCluster().isMigration() != getOldNetworkCluster().isMigration()) {
            networkClusterDao.setNetworkExclusivelyAsMigration(getClusterId(),
                    getNetworkCluster().isMigration() ? getPersistedNetwork().getId() : managementNetwork.getId());
        }

        if (getNetworkCluster().isGluster() != getOldNetworkCluster().isGluster()) {
            networkClusterDao.setNetworkExclusivelyAsGluster(getClusterId(),
                    getNetworkCluster().isGluster() ? getPersistedNetwork().getId() : null);
        }

        if (getNetworkCluster().isDefaultRoute() != getOldNetworkCluster().isDefaultRoute()) {
            networkClusterDao.setNetworkExclusivelyAsDefaultRoute(getClusterId(),
                    getNetworkCluster().isDefaultRoute() ? getPersistedNetwork().getId() : managementNetwork.getId());
        }

        networkClusterHelper.setStatus(getClusterId(), getPersistedNetwork());
        setSucceeded(true);
    }

    /**
     * Status is a calculated value, thus the value from the command input should be ignored.
     */
    private void preserveStatus() {
        getNetworkCluster().setStatus(getOldNetworkCluster().getStatus());
    }

    @Override
    protected boolean validate() {
        return validate(networkClusterAttachmentExists())
               && validateAttachment();
    }

    private boolean validateAttachment() {
        final UpdateNetworkClusterValidator networkClusterValidator = createNetworkClusterValidator();
        return validate(networkClusterValidator.managementNetworkUnset()) &&
                validate(networkClusterValidator.glusterNetworkInUseAndUnset(getCluster())) &&
                validateAttachment(networkClusterValidator);
    }

    private ValidationResult networkClusterAttachmentExists() {
        return getOldNetworkCluster() == null ?
                new ValidationResult(EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER) : ValidationResult.VALID;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER
                : AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return permissionsChecker.findPermissionCheckSubjects(getNetworkId(), getClusterId(), getActionType());
    }

    private Guid getNetworkId() {
        return getNetworkCluster() == null ? null : getNetworkCluster().getNetworkId();
    }

    /**
     * Checks the user has permissions either on Network or on Cluster for this action.<br>
     */
    @Override
    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        return permissionsChecker.checkPermissions(this, permSubjects);
    }

    private UpdateNetworkClusterValidator createNetworkClusterValidator() {
        return new UpdateNetworkClusterValidator(
                interfaceDao,
                networkDao,
                vdsDao,
                glusterBrickDao,
                getNetworkCluster(),
                getOldNetworkCluster());
    }
}
