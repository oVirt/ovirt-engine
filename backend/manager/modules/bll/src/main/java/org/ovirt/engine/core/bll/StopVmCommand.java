package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class StopVmCommand<T extends StopVmParameters> extends StopVmCommandBase<T> {

    public StopVmCommand(T stopVmParams,  CommandContext commandContext) {
        super(stopVmParams, commandContext);
    }

    protected StopVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void perform() {
        destroyVm();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (shouldSkipCommandExecutionCached()) {
            return logCommandExecutionSkipped("Stop VM");
        }
        if (getSuspendedVm()) {
            return getSucceeded() ? AuditLogType.USER_STOP_SUSPENDED_VM : AuditLogType.USER_STOP_SUSPENDED_VM_FAILED;
        } else {
            switch (getParameters().getStopVmType()) {
            case NORMAL:
                return getSucceeded() ? AuditLogType.USER_STOP_VM : AuditLogType.USER_FAILED_STOP_VM;

            case CANNOT_SHUTDOWN:
                return getSucceeded() ? AuditLogType.USER_STOPPED_VM_INSTEAD_OF_SHUTDOWN
                        : AuditLogType.USER_FAILED_STOPPING_VM_INSTEAD_OF_SHUTDOWN;

            default: // shouldn't get here:
                return AuditLogType.UNASSIGNED;
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__STOP);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }
}
