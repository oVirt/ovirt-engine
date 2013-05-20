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
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class LiveMigrateDisksTaskHandler implements SPMAsyncTaskHandler {

    private final TaskHandlerCommand<? extends LiveMigrateVmDisksParameters> enclosingCommand;
    private static final Log log = LogFactory.getLog(LiveMigrateDisksTaskHandler.class);

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

            enclosingCommand.getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());

            if (!parameters.getTaskGroupSuccess()) {
                ExecutionHandler.endTaskJob(commandContext.getExecutionContext(), false);
                log.errorFormat("Failed LiveMigrateDisk (Disk {0} , VM {1})",
                        parameters.getImageGroupID(),
                        parameters.getVmId());
            }
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
