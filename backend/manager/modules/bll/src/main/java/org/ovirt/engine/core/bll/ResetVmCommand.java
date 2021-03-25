package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ResetVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public ResetVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ResetVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__RESET);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.ResetVm, new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
            setActionReturnValue(returnValue.getReturnValue());
            setSucceeded(returnValue.getSucceeded());
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isVMResetSupported(getVm().getClusterCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RESET_VM_IS_NOT_SUPPORTED);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!getVm().isRunning() || getVm().getStatus() == VMStatus.WaitForLaunch) {
            return failVmStatusIllegal();
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_RESET_VM : AuditLogType.USER_FAILED_RESET_VM;
    }
}
