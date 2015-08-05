package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class FreezeVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public FreezeVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__ACTION__FREEZE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.Freeze,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        setActionReturnValue(returnValue.getReturnValue());
        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    protected boolean canDoAction() {
        final VM vm = getVm();
        if (vm == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().getStatus() != VMStatus.Up) {
            return failVmStatusIllegal();
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.FREEZE_VM_SUCCESS : AuditLogType.USER_FAILED_TO_FREEZE_VM;
    }
}
