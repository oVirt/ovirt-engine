package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.PauseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

public class PauseVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public PauseVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void perform() {
        if (getVm().isRunning()) {
            setActionReturnValue(runVdsCommand(VDSCommandType.Pause, new PauseVDSCommandParameters(getVdsId(), getVmId()))
                    .getReturnValue());
            // Vds.pause(VmId);
            setSucceeded(true);
        } else {
            setActionReturnValue(getVm().getStatus());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_PAUSE_VM : AuditLogType.USER_FAILED_PAUSE_VM;
    }

    private boolean canPauseVm(Guid vmId) {
        boolean retValue = true;
        VM vm = vmDao.get(vmId);
        if (vm == null) {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else {

            ValidationResult nonManagedVmValidationResult = VmHandler.canRunActionOnNonManagedVm(getVm(), this.getActionType());
            if (!nonManagedVmValidationResult.isValid()) {
                addValidationMessages(nonManagedVmValidationResult.getMessages());
                retValue = false;
            }

            if (retValue && (vm.getStatus() == VMStatus.WaitForLaunch || vm.getStatus() == VMStatus.MigratingFrom
                    || vm.getStatus() == VMStatus.NotResponding)) {
                retValue = failVmStatusIllegal();
            } else if (!vm.isRunning()) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
            }
        }

        if (!retValue) {
            addValidationMessage(EngineMessage.VAR__ACTION__PAUSE);
            addValidationMessage(EngineMessage.VAR__TYPE__VM);
        }
        return retValue;
    }

    @Override
    protected boolean validate() {
        return canPauseVm(getParameters().getVmId());
    }
}
