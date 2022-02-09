package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class ScreenshotVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public ScreenshotVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ScreenshotVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__SCREENSHOT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ScreenshotVm,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        setActionReturnValue(vdsReturnValue.getReturnValue());
        setSucceeded(vdsReturnValue.getSucceeded());
    }

    @Override
    protected boolean validate() {
        final VM vm = getVm();
        if (vm == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isVMScreenshotSupported(vm.getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SCREENSHOT_VM_IS_NOT_SUPPORTED);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!vm.isRunning() || vm.getStatus() == VMStatus.WaitForLaunch) {
            return failVmStatusIllegal();
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SCREENSHOT_VM_SUCCESS : AuditLogType.USER_FAILED_TO_SCREENSHOT_VM;
    }
}
