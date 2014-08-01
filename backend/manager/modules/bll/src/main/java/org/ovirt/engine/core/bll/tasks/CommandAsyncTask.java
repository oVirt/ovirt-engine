package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandMultiAsyncTasks;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * Base class for all tasks regarding a specific command.
 */
public class CommandAsyncTask extends SPMAsyncTask {
    private static final Object _lockObject = new Object();

    private static final Map<Guid, CommandMultiAsyncTasks> _multiTasksByCommandIds =
            new HashMap<Guid, CommandMultiAsyncTasks>();

    public CommandMultiAsyncTasks getCommandMultiAsyncTasks() {
        CommandMultiAsyncTasks entityInfo = null;
        synchronized (_lockObject) {
            entityInfo = _multiTasksByCommandIds.get(getCommandId());
        }
        return entityInfo;
    }

    public CommandAsyncTask(CommandCoordinator coco, AsyncTaskParameters parameters, boolean duringInit) {
        super(coco, parameters);
        boolean isNewCommandAdded = false;
        synchronized (_lockObject) {
            if (!_multiTasksByCommandIds.containsKey(getCommandId())) {
                log.infoFormat("CommandAsyncTask::Adding CommandMultiAsyncTasks object for command '{0}'",
                        getCommandId());
                _multiTasksByCommandIds.put(getCommandId(), new CommandMultiAsyncTasks(getCommandId()));
                isNewCommandAdded = true;
            }

            CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
            entityInfo.AttachTask(this);
        }

        if (duringInit && isNewCommandAdded) {
            CommandBase<?> command =
                    CommandsFactory.createCommand(parameters.getDbAsyncTask().getaction_type(),
                            parameters.getDbAsyncTask().getActionParameters());
            if (!command.acquireLockAsyncTask()) {
                log.warnFormat("Failed to acquire locks for command {0} with parameters {1}",
                        parameters.getDbAsyncTask().getaction_type(),
                        parameters.getDbAsyncTask().getActionParameters());
            }
        }


    }

    @Override
    public void concreteStartPollingTask() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        entityInfo.StartPollingTask(getVdsmTaskId());
    }

    @Override
    protected void onTaskEndSuccess() {
        logEndTaskSuccess();
        endActionIfNecessary();
    }

    private void endActionIfNecessary() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        if (entityInfo == null) {
            log.warnFormat(
                    "CommandAsyncTask::endActionIfNecessary: No info is available for entity '{0}', current task ('{1}') was probably created while other tasks were in progress, clearing task.",
                    getCommandId(),
                    getVdsmTaskId());

            clearAsyncTask();
        }

        else if (entityInfo.ShouldEndAction()) {
            log.infoFormat(
                    "CommandAsyncTask::endActionIfNecessary: All tasks of command '{0}' has ended -> executing 'endAction'",
                    getCommandId());

            log.infoFormat(
                    "CommandAsyncTask::endAction: Ending action for {0} tasks (command ID: '{1}'): calling endAction '.",
                    entityInfo.getTasksCountCurrentActionType(),
                    entityInfo.getCommandId());

            entityInfo.MarkAllWithAttemptingEndAction();
            ThreadPoolUtil.execute(new Runnable() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {
                    endCommandAction();
                }
            });
        }
    }

    private void endCommandAction() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        VdcReturnValueBase vdcReturnValue = null;
        ExecutionContext context = null;
        boolean endActionRuntimeException = false;

        AsyncTasks dbAsyncTask = getParameters().getDbAsyncTask();
        ArrayList<VdcActionParametersBase> imagesParameters = new ArrayList<VdcActionParametersBase>();
        for (EndedTaskInfo taskInfo : entityInfo.getEndedTasksInfo().getTasksInfo()) {
            VdcActionParametersBase childTaskParameters =
                    taskInfo.getTaskParameters().getDbAsyncTask().getTaskParameters();
            boolean childTaskGroupSuccess =
                    childTaskParameters.getTaskGroupSuccess() && taskInfo.getTaskStatus().getTaskEndedSuccessfully();
            childTaskParameters
                    .setTaskGroupSuccess(childTaskGroupSuccess);
            if (!childTaskParameters.equals(dbAsyncTask.getActionParameters())) {
                imagesParameters.add(childTaskParameters);
            }
        }
        dbAsyncTask.getActionParameters().setImagesParameters(imagesParameters);

        try {
            log.infoFormat("CommandAsyncTask::endCommandAction [within thread] context: Attempting to endAction '{0}', executionIndex: '{1}'",
                    dbAsyncTask.getActionParameters().getCommandType(),
                    dbAsyncTask.getActionParameters().getExecutionIndex());

            try {
                /**
                 * Creates context for the job which monitors the action
                 */
                Guid stepId = dbAsyncTask.getStepId();
                if (stepId != null) {
                    context = ExecutionHandler.createFinalizingContext(stepId);
                }
                vdcReturnValue = coco.endAction(this, context);
            } catch (VdcBLLException ex) {
                log.error(getErrorMessage());
                log.error(ex.toString());
                log.debug(ex);
            } catch (RuntimeException ex) {
                log.error(getErrorMessage(), ex);
                endActionRuntimeException = true;
            }
        }

        catch (RuntimeException Ex2) {
            log.error(
                    "CommandAsyncTask::endCommandAction [within thread]: An exception has been thrown (not related to 'endAction' itself)",
                    Ex2);
            endActionRuntimeException = true;
        }

        finally {
            // if a RuntimeExcpetion occurs we clear the task from db and perform no other action
            if (endActionRuntimeException) {
                handleEndActionRuntimeException(entityInfo, dbAsyncTask);
            } else {
                boolean isTaskGroupSuccess = dbAsyncTask.getActionParameters().getTaskGroupSuccess();
                handleEndActionResult(entityInfo, vdcReturnValue, context, isTaskGroupSuccess);
            }
        }
    }

    private VdcActionType getEndActionType(AsyncTasks dbAsyncTask) {
        VdcActionType commandType = dbAsyncTask.getActionParameters().getCommandType();
        if (!VdcActionType.Unknown.equals(commandType)) {
            return commandType;
        }
        return dbAsyncTask.getaction_type();
    }

    private String getErrorMessage() {
        return String.format("[within thread]: endAction for action type %1$s threw an exception.",
                getParameters().getDbAsyncTask().getActionParameters().getCommandType());
    }

    private void handleEndActionRuntimeException(CommandMultiAsyncTasks commandInfo, AsyncTasks dbAsyncTask) {
        try {
            VdcActionType actionType = getParameters().getDbAsyncTask().getaction_type();
            log.infoFormat(
                    "CommandAsyncTask::HandleEndActionResult: endAction for action type '{0}' threw an unrecoverable RuntimeException the task will be cleared.",
                    actionType);
            commandInfo.clearTaskByVdsmTaskId(dbAsyncTask.getVdsmTaskId());
            removeTaskFromDB();
            if (commandInfo.getAllCleared()) {
                log.infoFormat(
                        "CommandAsyncTask::HandleEndActionRuntimeException: Removing CommandMultiAsyncTasks object for entity '{0}'",
                        commandInfo.getCommandId());
                synchronized (_lockObject) {
                    _multiTasksByCommandIds.remove(commandInfo.getCommandId());
                }
            }
        }

        catch (RuntimeException ex) {
            log.error("CommandAsyncTask::HandleEndActionResult [within thread]: an exception has been thrown", ex);
        }
    }

    private void handleEndActionResult(CommandMultiAsyncTasks commandInfo, VdcReturnValueBase vdcReturnValue,
            ExecutionContext context,
            boolean isTaskGroupSuccess) {
        try {
            VdcActionType actionType = getParameters().getDbAsyncTask().getaction_type();
            log.infoFormat(
                    "CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type '{0}' completed, handling the result.",
                    actionType);

                if (vdcReturnValue == null || (!vdcReturnValue.getSucceeded() && vdcReturnValue.getEndActionTryAgain())) {
                    log.infoFormat(
                            "CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type {0} hasn't succeeded, not clearing tasks, will attempt again next polling.",
                        actionType);

                    commandInfo.Repoll();
                }

                else {
                    log.infoFormat(
                            "CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type {0} {1}succeeded, clearing tasks.",
                        actionType,
                            (vdcReturnValue.getSucceeded() ? "" : "hasn't "));

                    /**
                     * Terminate the job by the return value of endAction.
                     * The operation will end also the FINALIZING step.
                     */
                    if (context != null) {
                        ExecutionHandler.endTaskJob(context, vdcReturnValue.getSucceeded() && isTaskGroupSuccess);
                    }

                    commandInfo.ClearTasks();

                    synchronized (_lockObject) {
                        if (commandInfo.getAllCleared()) {
                            log.infoFormat(
                                    "CommandAsyncTask::HandleEndActionResult [within thread]: Removing CommandMultiAsyncTasks object for entity '{0}'",
                                    commandInfo.getCommandId());
                            _multiTasksByCommandIds.remove(commandInfo.getCommandId());
                        }
                    }
                }
        }

        catch (RuntimeException ex) {
            log.error("CommandAsyncTask::HandleEndActionResult [within thread]: an exception has been thrown", ex);
        }
    }

    @Override
    protected void onTaskEndFailure() {
        logEndTaskFailure();
        endActionIfNecessary();
    }

    @Override
    protected void onTaskDoesNotExist() {
        logTaskDoesntExist();
        endActionIfNecessary();
    }


    private static final Log log = LogFactory.getLog(CommandAsyncTask.class);
}
