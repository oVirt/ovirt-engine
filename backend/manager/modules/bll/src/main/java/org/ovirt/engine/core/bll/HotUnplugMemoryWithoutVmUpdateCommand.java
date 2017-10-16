package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.HotUnplugMemoryWithoutVmUpdateParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * It tries to hot unplug memory device of a VM.
 */
@NonTransactiveCommandAttribute
public class HotUnplugMemoryWithoutVmUpdateCommand<P extends HotUnplugMemoryWithoutVmUpdateParameters>
        extends HotUnplugMemoryCommandBase<P> {

    public HotUnplugMemoryWithoutVmUpdateCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getParameters().getMinMemoryMb() > getVm().getMemSizeMb()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (getParameters().getMinMemoryMb() < 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_BE_NEGATIVE,
                    ReplacementUtils.createSetVariableString("minMemoryMb", getParameters().getMinMemoryMb()));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        executeHotUnplug(getParameters().getMinMemoryMb());
        setSucceeded(true);
    }
}
