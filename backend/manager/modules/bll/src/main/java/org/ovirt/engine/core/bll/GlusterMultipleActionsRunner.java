package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public class GlusterMultipleActionsRunner extends MultipleActionsRunner {

    public GlusterMultipleActionsRunner(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected VdcReturnValueBase runCanDoActionOnly(final int currentCanDoActionId, final int totalSize) {
        try {
            return super.runCanDoActionOnly(currentCanDoActionId, totalSize);
        } finally {
            // free the lock so that canDoActionOnly() on next command doesn't block
            getCommands().get(currentCanDoActionId).freeLock();
        }
    }

    @Override
    protected void executeValidatedCommand(CommandBase<?> command) {
        // Since we had released the lock at the end of CanDoAction,
        // it must be acquired back just before execution of the command
        command.acquireLock();
        super.executeValidatedCommand(command);
    }
}
