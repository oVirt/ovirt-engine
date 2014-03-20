package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
public class HotUnPlugDiskFromVmCommand<T extends HotPlugDiskToVmParameters> extends HotPlugDiskToVmCommand<T> {

    public HotUnPlugDiskFromVmCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HOT_UNPLUG);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_HOTUNPLUG_DISK : AuditLogType.USER_FAILED_HOTUNPLUG_DISK;
    }

    @Override
    protected VDSCommandType getPlugAction() {
        return VDSCommandType.HotUnPlugDisk;
    }
}
