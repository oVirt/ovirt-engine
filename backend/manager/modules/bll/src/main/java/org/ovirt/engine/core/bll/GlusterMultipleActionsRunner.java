package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public class GlusterMultipleActionsRunner extends MultipleActionsRunner {

    public GlusterMultipleActionsRunner(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    protected void runCanDoActionOnly(final ArrayList<VdcReturnValueBase> returnValues,
            final int currentCanDoActionId, final int totalSize, final CountDownLatch latch) {
        try {
            super.runCanDoActionOnly(returnValues, currentCanDoActionId, totalSize, latch);
        } finally {
            // free the lock so that canDoActionOnly() on next command doesn't block
            getCommands().get(currentCanDoActionId).freeLock();
        }
    }

    @Override
    protected void executeValidatedCommands(CommandBase<?> command) {
        // Since we had released the lock at the end of CanDoAction,
        // it must be acquired back just before execution of the command
        command.acquireLock();
        super.executeValidatedCommands(command);
    }
}
