package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class EnableGlusterHookCommand<T extends GlusterHookParameters> extends GlusterHookStatusChangeCommand<T> {

    public EnableGlusterHookCommand(T params) {
        super(params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ENABLE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_HOOK);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (errors.isEmpty()) {
                return AuditLogType.GLUSTER_HOOK_ENABLE;
            } else {
                return AuditLogType.GLUSTER_HOOK_ENABLE_PARTIAL;
            }
        } else {
            return errorType == null ? AuditLogType.GLUSTER_HOOK_ENABLE_FAILED : errorType;
        }
    }

    @Override
    protected VDSCommandType getStatusChangeVDSCommand() {
        return VDSCommandType.EnableGlusterHook;

    }

    @Override
    protected GlusterHookStatus getNewStatus() {
        return GlusterHookStatus.ENABLED;
    }




}
