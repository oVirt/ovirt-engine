package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveVmFromPoolCommand<T extends RemoveVmFromPoolParameters> extends VmPoolCommandBase<T> {

    public RemoveVmFromPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getVmId());
        if (getVm() != null) {
            setVmPoolId(getVm().getVmPoolId());
        }
    }

    public RemoveVmFromPoolCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVmPoolId() == null) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL);
        }

        if (getVm().isRunningOrPaused() || getVm().getStatus() == VMStatus.Unknown) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            getVmPoolDAO().removeVmFromVmPool(getVmId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FROM_POOL : AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED;
    }
}
