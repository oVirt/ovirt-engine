package org.ovirt.engine.core.bll.exportimport;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@NonTransactiveCommandAttribute
public class CancelConvertVmCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    @Inject
    protected ResourceManager resourceManager;

    public CancelConvertVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVdsId(getVmManager().getConvertProxyHostId());
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue retVal = runVdsCommand(
                VDSCommandType.CancelConvertVm,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));

        setSucceeded(retVal.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
        addValidationMessage(EngineMessage.VAR__ACTION__CANCEL_CONVERSION);
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (Guid.isNullOrEmpty(getVdsId())) {
            return failValidation(EngineMessage.VM_CANNOT_CANCEL_CONVERSION_WHEN_VM_IS_NOT_BEING_CONVERTED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                AuditLogType.VM_CANCEL_CONVERSION
                : AuditLogType.VM_CANCEL_CONVERSION_FAILED;
    }

    protected VmManager getVmManager() {
        return resourceManager.getVmManager(getVmId());
    }
}
