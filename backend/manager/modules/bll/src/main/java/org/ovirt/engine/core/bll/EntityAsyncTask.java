package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
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
public class EntityAsyncTask extends SPMAsyncTask {
    private static final Object _lockObject = new Object();

    private static final Map<Guid, EntityMultiAsyncTasks> _multiTasksByCommandIds =
            new HashMap<Guid, EntityMultiAsyncTasks>();

    private EntityMultiAsyncTasks GetEntityMultiAsyncTasks() {
        EntityMultiAsyncTasks entityInfo = null;
        synchronized (_lockObject) {
            entityInfo = _multiTasksByCommandIds.get(getCommandId());
        }
        return entityInfo;
    }

    public EntityAsyncTask(AsyncTaskParameters parameters, boolean duringInit) {
        super(parameters);
        boolean isNewCommandAdded = false;
        synchronized (_lockObject) {
            if (!_multiTasksByCommandIds.containsKey(getCommandId())) {
                log.infoFormat("EntityAsyncTask::Adding EntityMultiAsyncTasks object for command '{0}'",
                        getCommandId());
                _multiTasksByCommandIds.put(getCommandId(), new EntityMultiAsyncTasks(getCommandId()));
                isNewCommandAdded = true;
            }
        }
        if (duringInit && isNewCommandAdded) {
            CommandBase<?> command =
                    CommandsFactory.CreateCommand(parameters.getDbAsyncTask().getaction_type(),
                            parameters.getDbAsyncTask().getActionParameters());
            if (!command.acquireLockAsyncTask()) {
                log.warnFormat("Failed to acquire locks for command {0} with parameters {1}",
                        parameters.getDbAsyncTask().getaction_type(),
                        parameters.getDbAsyncTask().getActionParameters());
            }
        }

        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        entityInfo.AttachTask(this);
    }

    @Override
    protected void ConcreteStartPollingTask() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        entityInfo.StartPollingTask(getVdsmTaskId());
    }

    @Override
    protected void OnTaskEndSuccess() {
        LogEndTaskSuccess();
        EndActionIfNecessary();
    }

    private void EndActionIfNecessary() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        if (entityInfo == null) {
            log.warnFormat(
                    "EntityAsyncTask::EndActionIfNecessary: No info is available for entity '{0}', current task ('{1}') was probably created while other tasks were in progress, clearing task.",
                    getCommandId(),
                    getVdsmTaskId());

            clearAsyncTask();
        }

        else if (entityInfo.ShouldEndAction()) {
            log.infoFormat(
                    "EntityAsyncTask::EndActionIfNecessary: All tasks of entity '{0}' has ended -> executing 'EndAction'",
                    getCommandId());

            log.infoFormat(
                    "EntityAsyncTask::EndAction: Ending action for {0} tasks (command ID: '{1}'): calling EndAction '.",
                    entityInfo.getTasksCountCurrentActionType(),
                    entityInfo.getCommandId());

            entityInfo.MarkAllWithAttemptingEndAction();
            ThreadPoolUtil.execute(new Runnable() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {
                    EndCommandAction();
                }
            });
        }
    }

    private void EndCommandAction() {
        EntityMultiAsyncTasks entityInfo = GetEntityMultiAsyncTasks();
        VdcReturnValueBase vdcReturnValue = null;
        ExecutionContext context = null;

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
            log.infoFormat("EntityAsyncTask::EndCommandAction [within thread] context: Attempting to EndAction '{0}', executionIndex: '{1}'",
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

                vdcReturnValue =
                        Backend.getInstance().endAction(getEndActionType(dbAsyncTask),
                                dbAsyncTask.getActionParameters(),
                                new CommandContext(context));
            } catch (VdcBLLException ex) {
                log.error(getErrorMessage());
                log.error(ex.toString());
                log.debug(ex);
            } catch (RuntimeException ex) {
                log.error(getErrorMessage(), ex);
            }
        }

        catch (RuntimeException Ex2) {
            log.error(
                    "EntityAsyncTask::EndCommandAction [within thread]: An exception has been thrown (not related to 'EndAction' itself)",
                    Ex2);
        }

        finally {
            boolean isTaskGroupSuccess = dbAsyncTask.getActionParameters().getTaskGroupSuccess();
            handleEndActionResult(entityInfo, vdcReturnValue, context, isTaskGroupSuccess);
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
        return String.format("[within thread]: EndAction for action type %1$s threw an exception.",
                getParameters().getDbAsyncTask().getActionParameters().getCommandType());
    }

    private void handleEndActionResult(EntityMultiAsyncTasks commandInfo, VdcReturnValueBase vdcReturnValue,
            ExecutionContext context,
            boolean isTaskGroupSuccess) {
        try {
            VdcActionType actionType = getParameters().getDbAsyncTask().getaction_type();
            log.infoFormat(
                    "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type '{0}' completed, handling the result.",
                    actionType);

                if (vdcReturnValue == null || (!vdcReturnValue.getSucceeded() && vdcReturnValue.getEndActionTryAgain())) {
                    log.infoFormat(
                            "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type {0} hasn't succeeded, not clearing tasks, will attempt again next polling.",
                        actionType);

                    commandInfo.Repoll();
                }

                else {
                    log.infoFormat(
                            "EntityAsyncTask::HandleEndActionResult [within thread]: EndAction for action type {0} {1}succeeded, clearing tasks.",
                        actionType,
                            (vdcReturnValue.getSucceeded() ? "" : "hasn't "));

                    /**
                     * Terminate the job by the return value of EndAction.
                     * The operation will end also the FINALIZING step.
                     */
                    if (context != null) {
                        ExecutionHandler.endTaskJob(context, vdcReturnValue.getSucceeded() && isTaskGroupSuccess);
                    }

                    commandInfo.ClearTasks();

                    synchronized (_lockObject) {
                        if (commandInfo.getAllCleared()) {
                            log.infoFormat(
                                    "EntityAsyncTask::HandleEndActionResult [within thread]: Removing EntityMultiAsyncTasks object for entity '{0}'",
                                    commandInfo.getCommandId());
                            _multiTasksByCommandIds.remove(commandInfo.getCommandId());
                        }
                    }
                }
        }

        catch (RuntimeException ex) {
            log.error("EntityAsyncTask::HandleEndActionResult [within thread]: an exception has been thrown", ex);
        }
    }

    @Override
    protected void OnTaskEndFailure() {
        LogEndTaskFailure();
        EndActionIfNecessary();
    }

    @Override
    protected void OnTaskDoesNotExist() {
        LogTaskDoesntExist();
        EndActionIfNecessary();
    }


    private static final Log log = LogFactory.getLog(EntityAsyncTask.class);
}
