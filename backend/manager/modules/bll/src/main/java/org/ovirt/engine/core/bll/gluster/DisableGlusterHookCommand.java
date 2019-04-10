package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

/**
 * BLL command to Disable Gluster hook
 */
@NonTransactiveCommandAttribute
public class DisableGlusterHookCommand<T extends GlusterHookParameters> extends GlusterHookStatusChangeCommand<T> {
    private static final long serialVersionUID = 2267182025441596357L;


    public DisableGlusterHookCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__DISABLE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_HOOK);
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (errors.isEmpty()) {
                return AuditLogType.GLUSTER_HOOK_DISABLE;
            } else {
                return AuditLogType.GLUSTER_HOOK_DISABLE_PARTIAL;
            }
        } else {
            return errorType == null ? AuditLogType.GLUSTER_HOOK_DISABLE_FAILED : errorType;
        }
    }

    @Override
    protected VDSCommandType getStatusChangeVDSCommand() {
        return VDSCommandType.DisableGlusterHook;
    }

    @Override
    protected GlusterHookStatus getNewStatus() {
        return GlusterHookStatus.DISABLED;
    }

}
