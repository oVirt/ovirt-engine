package org.ovirt.engine.core.bll.lsm;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;

public class LiveMigrateDisksTaskHandler implements SPMAsyncTaskHandler {

    private final TaskHandlerCommand<? extends LiveMigrateVmDisksParameters> enclosingCommand;

    public LiveMigrateDisksTaskHandler(TaskHandlerCommand<? extends LiveMigrateVmDisksParameters> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    @Override
    public void execute() {
        for (LiveMigrateDiskParameters parameters : enclosingCommand.getParameters().getParametersList()) {
            CommandContext commandContext = ExecutionHandler.createInternalJobContext();
            ExecutionHandler.setAsyncJob(commandContext.getExecutionContext(), true);
            parameters.setSessionId(enclosingCommand.getParameters().getSessionId());

            VdcReturnValueBase vdcReturnValue =
                    Backend.getInstance().runInternalAction(VdcActionType.LiveMigrateDisk,
                            parameters,
                            commandContext);

            enclosingCommand.getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
        }
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void endSuccessfully() {
    }

    @Override
    public void endWithFailure() {
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public AsyncTaskType getTaskType() {
        // No implementation - handled by the command
        return null;
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        // No implementation - there is no live-merge
        return null;
    }

    @Override
    public void compensate() {
    }
}
