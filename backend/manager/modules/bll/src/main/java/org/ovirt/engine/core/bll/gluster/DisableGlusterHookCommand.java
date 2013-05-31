package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to Disable Gluster hook
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class DisableGlusterHookCommand<T extends GlusterHookParameters> extends GlusterHookStatusChangeCommand<T> {
    private static final long serialVersionUID = 2267182025441596357L;


    public DisableGlusterHookCommand(T params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DISABLE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_HOOK);
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
