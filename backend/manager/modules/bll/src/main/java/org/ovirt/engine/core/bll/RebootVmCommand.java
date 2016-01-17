package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RebootVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    public RebootVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__RESTART);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        if (isColdReboot()) {
            VdcReturnValueBase returnValue = runInternalAction(VdcActionType.ShutdownVm, new ShutdownVmParameters(getVmId(), false));
            setReturnValue(returnValue);
            setSucceeded(returnValue.getSucceeded());
            if (getSucceeded()) {
                resourceManager.getVmManager(getVmId()).setColdReboot(true);
            }
        } else {
            final VDSReturnValue returnValue = runVdsCommand(VDSCommandType.RebootVm, new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
            setActionReturnValue(returnValue.getReturnValue());
            setSucceeded(returnValue.getSucceeded());
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.PoweringUp) {
            return failVmStatusIllegal();
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REBOOT_VM : AuditLogType.USER_FAILED_REBOOT_VM;
    }

    private boolean isColdReboot() {
        return getVm().isRunOnce() || getVm().isNextRunConfigurationExists();
    }
}
