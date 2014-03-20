package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterClusterParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;


/**
 * BLL command to refresh gluster hooks in a cluster
 */
@NonTransactiveCommandAttribute
public class RefreshGlusterHooksCommand<T extends GlusterClusterParameters> extends GlusterCommandBase<T> {

    public RefreshGlusterHooksCommand(T params) {
        super(params);
        setVdsGroupId(params.getClusterId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REFRESH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_HOOK);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getClusterId() == null || getVdsGroup() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if(!super.canDoAction()) {
            return false;
        }

        return true;
    }

    protected GlusterHookSyncJob getSyncJobInstance() {
        return GlusterHookSyncJob.getInstance();
    }

    @Override
    protected void executeCommand() {
        getSyncJobInstance().refreshHooksInCluster(getVdsGroup(), true);
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_HOOK_REFRESH;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_HOOK_REFRESH_FAILED : errorType;
        }
    }
}
