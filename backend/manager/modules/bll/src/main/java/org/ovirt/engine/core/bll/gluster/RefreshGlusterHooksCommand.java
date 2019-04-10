package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterClusterParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * BLL command to refresh gluster hooks in a cluster
 */
@NonTransactiveCommandAttribute
public class RefreshGlusterHooksCommand<T extends GlusterClusterParameters> extends GlusterCommandBase<T> {
    @Inject
    private GlusterHookSyncJob glusterHookSyncJob;

    public RefreshGlusterHooksCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
        setClusterId(params.getClusterId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REFRESH);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_HOOK);
    }

    @Override
    protected boolean validate() {
        if (getParameters().getClusterId() == null || getCluster() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if(!super.validate()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        glusterHookSyncJob.refreshHooksInCluster(getCluster(), true);
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
