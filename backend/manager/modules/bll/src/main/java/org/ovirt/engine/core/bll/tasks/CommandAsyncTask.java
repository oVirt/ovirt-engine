package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandMultiAsyncTasks;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all tasks regarding a specific command.
 */
public class CommandAsyncTask extends SPMAsyncTask {
    private static final Logger log = LoggerFactory.getLogger(CommandAsyncTask.class);

    private static final Object _lockObject = new Object();

    private static final Map<Guid, CommandMultiAsyncTasks> _multiTasksByCommandIds = new HashMap<>();

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
                log.info("CommandAsyncTask::Adding CommandMultiAsyncTasks object for command '{}'",
                        getCommandId());
                _multiTasksByCommandIds.put(getCommandId(), new CommandMultiAsyncTasks(getCommandId()));
                isNewCommandAdded = true;
            }

            CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
            entityInfo.attachTask(this);
        }

        if (duringInit && isNewCommandAdded) {
            CommandBase<?> command =
                    CommandsFactory.createCommand(parameters.getDbAsyncTask().getActionType(),
                            parameters.getDbAsyncTask().getActionParameters());
            if (!command.acquireLockAsyncTask()) {
                log.warn("Failed to acquire locks for command '{}' with parameters '{}'",
                        parameters.getDbAsyncTask().getActionType(),
                        parameters.getDbAsyncTask().getActionParameters());
            }
        }


    }

    @Override
    public void concreteStartPollingTask() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        entityInfo.startPollingTask(getVdsmTaskId());
    }

    @Override
    protected void onTaskEndSuccess() {
        logEndTaskSuccess();
        endActionIfNecessary();
    }

    private void endActionIfNecessary() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        if (entityInfo == null) {
            log.warn("CommandAsyncTask::endActionIfNecessary: No info is available for entity '{}', current"
                            + " task ('{}') was probably created while other tasks were in progress, clearing task.",
                    getCommandId(),
                    getVdsmTaskId());

            clearAsyncTask();
        } else if (entityInfo.shouldEndAction() && !hasRunningChildCommands()) {
            log.info(
                    "CommandAsyncTask::endActionIfNecessary: All tasks of command '{}' has ended -> executing 'endAction'",
                    getCommandId());

            log.info("CommandAsyncTask::endAction: Ending action for '{}' tasks (command ID: '{}'): calling endAction '.",
                    entityInfo.getTasksCountCurrentActionType(),
                    entityInfo.getCommandId());

            entityInfo.markAllWithAttemptingEndAction();
            ThreadPoolUtil.execute(() -> endCommandAction());
        }
    }

    private boolean hasRunningChildCommands() {
        Guid rootCmdId = getParameters().getDbAsyncTask().getRootCommandId();
        for (CommandEntity entity : coco.getChildCmdsByRootCmdId(rootCmdId)) {
            if (!hasCompleted(entity) && !coco.doesCommandContainAsyncTask(entity.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCompleted(CommandEntity entity) {
        return CommandStatus.SUCCEEDED.equals(entity.getCommandStatus()) ||
                CommandStatus.FAILED.equals(entity.getCommandStatus()) ||
                CommandStatus.ENDED_WITH_FAILURE.equals(entity.getCommandStatus()) ||
                CommandStatus.ENDED_SUCCESSFULLY.equals(entity.getCommandStatus()) ||
                CommandStatus.EXECUTION_FAILED.equals(entity.getCommandStatus());
    }

    private void endCommandAction() {
        CommandMultiAsyncTasks entityInfo = getCommandMultiAsyncTasks();
        ActionReturnValue actionReturnValue = null;
        boolean endActionRuntimeException = false;

        AsyncTask dbAsyncTask = getParameters().getDbAsyncTask();
        ArrayList<ActionParametersBase> imagesParameters = new ArrayList<>();
        for (EndedTaskInfo taskInfo : entityInfo.getEndedTasksInfo().getTasksInfo()) {
            ActionParametersBase childTaskParameters =
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
            log.info("CommandAsyncTask::endCommandAction [within thread] context: Attempting to endAction '{}',",
                    dbAsyncTask.getActionParameters().getCommandType());

            try {
                actionReturnValue = coco.endAction(this);
            } catch (EngineException ex) {
                log.error("{}: {}", getErrorMessage(), ex.getMessage());
                log.debug("Exception", ex);
            } catch (RuntimeException ex) {
                log.error(getErrorMessage(), ex);
                endActionRuntimeException = true;
            }
        } catch (RuntimeException Ex2) {
            log.error("CommandAsyncTask::endCommandAction [within thread]: An exception has been thrown (not"
                            + " related to 'endAction' itself)",
                    Ex2);
            endActionRuntimeException = true;
        } finally {
            // if a RuntimeExcpetion occurs we clear the task from db and perform no other action
            if (endActionRuntimeException) {
                handleEndActionRuntimeException(entityInfo, dbAsyncTask);
            } else {
                handleEndActionResult(entityInfo, actionReturnValue);
            }
        }
    }

    private String getErrorMessage() {
        return String.format("[within thread]: endAction for action type %1$s threw an exception.",
                getParameters().getDbAsyncTask().getActionParameters().getCommandType());
    }

    private void handleEndActionRuntimeException(CommandMultiAsyncTasks commandInfo, AsyncTask dbAsyncTask) {
        try {
            ActionType actionType = getParameters().getDbAsyncTask().getActionType();
            log.info("CommandAsyncTask::HandleEndActionResult: endAction for action type '{}' threw an"
                            + " unrecoverable RuntimeException the task will be cleared.",
                    actionType);
            commandInfo.clearTaskByVdsmTaskId(dbAsyncTask.getVdsmTaskId());
            removeTaskFromDB();
            if (commandInfo.getAllCleared()) {
                log.info("CommandAsyncTask::HandleEndActionRuntimeException: Removing CommandMultiAsyncTasks"
                                + " object for entity '{}'",
                        commandInfo.getCommandId());
                synchronized (_lockObject) {
                    _multiTasksByCommandIds.remove(commandInfo.getCommandId());
                }
            }
        } catch (RuntimeException ex) {
            log.error("CommandAsyncTask::HandleEndActionResult [within thread]: an exception has been thrown", ex);
        }
    }

    private void handleEndActionResult(CommandMultiAsyncTasks commandInfo, ActionReturnValue actionReturnValue) {
        try {
            ActionType actionType = getParameters().getDbAsyncTask().getActionType();
            log.info("CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type '{}'"
                            + " completed, handling the result.",
                    actionType);

                if (actionReturnValue == null || (!actionReturnValue.getSucceeded() && actionReturnValue.getEndActionTryAgain())) {
                    log.info("CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type"
                                    + " '{}' hasn't succeeded, not clearing tasks, will attempt again next polling.",
                        actionType);

                    commandInfo.repoll();
                } else {
                    log.info("CommandAsyncTask::HandleEndActionResult [within thread]: endAction for action type"
                                    + " '{}' {}succeeded, clearing tasks.",
                        actionType,
                        actionReturnValue.getSucceeded() ? "" : "hasn't ");

                    commandInfo.clearTasks();

                    synchronized (_lockObject) {
                        if (commandInfo.getAllCleared()) {
                            log.info("CommandAsyncTask::HandleEndActionResult [within thread]: Removing"
                                            + " CommandMultiAsyncTasks object for entity '{}'",
                                    commandInfo.getCommandId());
                            _multiTasksByCommandIds.remove(commandInfo.getCommandId());
                        }
                    }
                }
        } catch (RuntimeException ex) {
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
}
