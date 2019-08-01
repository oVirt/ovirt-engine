package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;

@Singleton
public class CoCoAsyncTaskHelper {

    @Inject
    private AsyncTaskFactory asyncTaskFactory;
    @Inject
    private AsyncTaskUtils asyncTaskUtils;
    @Inject
    private Instance<CommandCoordinator> coco;
    @Inject
    private Instance<CommandsRepository> commandsRepositoryInstance;
    @Inject
    private Instance<AsyncTaskManager> asyncTaskManager;
    @Inject
    private VDSBrokerFrontend resourceManager;
    @Inject
    private AsyncTaskDao asyncTaskDao;
    @Inject
    private ExecutionHandler executionHandler;

    /**
     * Use this method in order to create task in the AsyncTaskManager in a safe way. If you use
     * this method within a certain command, make sure that the command implemented the
     * ConcreteCreateTask method.
     *
     * @param asyncTaskCreationInfo
     *            info to send to AsyncTaskManager when creating the task.
     * @param parentCommand
     *            ActionType of the command that its EndAction we want to invoke when tasks are finished.
     * @param entitiesMap
     *            Map of entities that are associated with the task
     * @return Guid of the created task.
     */
    public Guid createTask(Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap) {
        Step taskStep =
                executionHandler.addTaskStep(command.getExecutionContext(),
                        StepEnum.getStepNameByTaskType(asyncTaskCreationInfo.getTaskType()),
                        description,
                        command.getCommandStepSubjectEntities());
        command.getExecutionContext().setStep(taskStep);
        if (taskStep != null) {
            asyncTaskCreationInfo.setStepId(taskStep.getId());
        }
        SPMAsyncTask task = concreteCreateTask(taskId, command, asyncTaskCreationInfo, parentCommand);
        task.setEntitiesMap(entitiesMap);
        asyncTaskUtils.addOrUpdateTaskInDB(coco.get(), task);
        asyncTaskManager.get().lockAndAddTaskToManager(task);
        Guid vdsmTaskId = task.getVdsmTaskId();
        executionHandler.updateStepExternalId(taskStep, vdsmTaskId, ExternalSystemType.VDSM);
        return vdsmTaskId;

    }

    public SPMAsyncTask createTask(AsyncTaskType taskType,
        AsyncTaskParameters taskParameters) {
        return asyncTaskFactory.construct(taskType, taskParameters, false);
    }

    /**
     * Create the {@link SPMAsyncTask} object to be run
     * @param taskId the id of the async task place holder in the database
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     * @return An {@link SPMAsyncTask} object representing the task to be run
     */
    public SPMAsyncTask concreteCreateTask(Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        AsyncTaskParameters p =
                new AsyncTaskParameters(asyncTaskCreationInfo,
                        getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand));
        p.setEntityInfo(command.getParameters().getEntityInfo());
        return createTask(internalGetTaskType(command), p);
    }

    public void revertTasks(CommandBase<?> command) {
        if (command.getParameters().getVdsmTaskIds() != null) {
            // list to send to the pollTasks method
            ArrayList<Guid> taskIdAsList = new ArrayList<>();

            for (Guid taskId : command.getParameters().getVdsmTaskIds()) {
                taskIdAsList.add(taskId);
                ArrayList<AsyncTaskStatus> tasksStatuses = asyncTaskManager.get().pollTasks(
                        taskIdAsList);
                // call revert task only if ended successfully
                if (tasksStatuses.get(0).getTaskEndedSuccessfully()) {
                    resourceManager.runVdsCommand(
                            VDSCommandType.SPMRevertTask,
                            new SPMTaskGuidBaseVDSCommandParameters(
                                    command.getStoragePool().getId(), taskId));
                }
                taskIdAsList.clear();
            }
        }
    }

    public void cancelTasks(final CommandBase<?> command, final Logger log) {
        if (command.hasTasks()) {
            ThreadPoolUtil.execute(() -> {
                log.info("Rollback for command '{}'", command.getClass().getName());
                try {
                    asyncTaskManager.get().cancelTasks(command.getReturnValue().getVdsmTaskIdList());
                } catch (Exception e) {
                    log.error("Failed to cancel tasks for command '{}'",
                            command.getClass().getName());
                }
            });
        }
    }

    public List<AsyncTask> getAllAsyncTasksFromDb() {
        List<AsyncTask> asyncTasks = asyncTaskDao.getAll();
        for (AsyncTask asyncTask : asyncTasks) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTasks;
    }

    public CommandEntity createCommandEntity(Guid cmdId, ActionType actionType, ActionParametersBase params) {
        CommandEntity cmdEntity = new CommandEntity();
        cmdEntity.setId(cmdId);
        cmdEntity.setCommandType(actionType);
        cmdEntity.setCommandParameters(params);
        return cmdEntity;
    }

    private CommandEntity getCommandEntity(Guid cmdId) {
        CommandEntity cmdEntity = commandsRepositoryInstance.get().getCommandEntity(cmdId);
        if (cmdEntity == null) {
            cmdEntity = createCommandEntity(cmdId, ActionType.Unknown, new ActionParametersBase());
        }
        return cmdEntity;
    }

    /**
     * This method is always called from with in a transaction
     */
    public void saveAsyncTaskToDb(final AsyncTask asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            asyncTaskDao.save(asyncTask);
            coco.get().persistCommand(asyncTask.getRootCmdEntity());
            coco.get().persistCommand(asyncTask.getChildCmdEntity());
            return null;
        });
    }

    public AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
        AsyncTask asyncTask = asyncTaskDao.get(asyncTaskId);
        if (asyncTask != null) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTask;
    }

    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            AsyncTask asyncTask = asyncTaskDao.get(taskId);
            int retVal = asyncTaskDao.remove(taskId);
            if (shouldRemoveCommand(asyncTask)) {
                coco.get().removeCommand(asyncTask.getCommandId());
                if (!coco.get().hasCommandEntitiesWithRootCommandId(asyncTask.getRootCommandId())) {
                    coco.get().removeCommand(asyncTask.getRootCommandId());
                }
            }

            return retVal;
        });
    }

    private boolean shouldRemoveCommand(AsyncTask asyncTask) {
        if (asyncTask == null || Guid.isNullOrEmpty(asyncTask.getCommandId())) {
            return false;
        }

        CommandEntity cmdEntity = coco.get().getCommandEntity(asyncTask.getCommandId());
        CommandEntity parentEntity = null;
        if (cmdEntity != null) {
            parentEntity = coco.get().getCommandEntity(cmdEntity.getParentCommandId());
        }
        return cmdEntity != null && !cmdEntity.isCallbackEnabled() &&
                (parentEntity == null || !parentEntity.isCallbackEnabled());
    }

    public AsyncTask getByVdsmTaskId(Guid vdsmTaskId) {
        AsyncTask asyncTask = asyncTaskDao.getByVdsmTaskId(vdsmTaskId);
        if (asyncTask != null) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTask;
    }

    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            AsyncTask asyncTask = asyncTaskDao.getByVdsmTaskId(vdsmTaskId);
            int retVal = asyncTaskDao.removeByVdsmTaskId(vdsmTaskId);
            if (shouldRemoveCommand(asyncTask)) {
                coco.get().removeCommand(asyncTask.getCommandId());
                if (!coco.get().hasCommandEntitiesWithRootCommandId(asyncTask.getRootCommandId())) {
                    coco.get().removeCommand(asyncTask.getRootCommandId());
                }
            }

            return retVal;
        });
    }

    public void addOrUpdateTaskInDB(final AsyncTask asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            if (asyncTask.getChildCmdEntity().getRootCommandId() != null &&
                    !asyncTask.getChildCmdEntity().getRootCommandId().equals(asyncTask.getChildCmdEntity().getId())) {
                coco.get().persistCommand(asyncTask.getRootCmdEntity());
            }
            coco.get().persistCommand(asyncTask.getChildCmdEntity());
            asyncTaskDao.saveOrUpdate(asyncTask);
            return null;
        });
    }

    public AsyncTask getAsyncTask(Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        AsyncTask asyncTask = null;
        if (!taskId.equals(Guid.Empty)) {
            asyncTask = getAsyncTaskFromDb(taskId);
        }
        if (asyncTask != null) {
            ActionParametersBase parentParameters = command.getParentParameters() == null ?
                    command.getParentParameters(parentCommand) : command.getParentParameters();
            Guid parentCommandId =
                    parentParameters == null ? Guid.Empty : parentParameters.getCommandId();
            if (ActionType.Unknown.equals(command.getParameters().getCommandType())) {
                command.getParameters().setCommandType(command.getActionType());
            }
            asyncTask.setActionType(parentCommand);
            asyncTask.setVdsmTaskId(asyncTaskCreationInfo.getVdsmTaskId());
            asyncTask.setActionParameters(parentParameters);
            asyncTask.setTaskParameters(command.getParameters());
            asyncTask.setStepId(asyncTaskCreationInfo.getStepId());
            asyncTask.setCommandId(command.getCommandId());
            asyncTask.setRootCmdEntity(getParentCommandEntity(parentCommandId,
                    parentCommand,
                    parentParameters));
            asyncTask.setChildCmdEntity(getChildCommandEntity(command,
                    parentCommand));
            asyncTask.setStoragePoolId(asyncTaskCreationInfo.getStoragePoolID());
            asyncTask.setTaskType(asyncTaskCreationInfo.getTaskType());
            asyncTask.setCommandStatus(command.getCommandStatus());
            asyncTask.setCommandType(command.getParameters().getCommandType());
        } else {
            asyncTask = createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
        }
        return asyncTask;
    }

    public AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        ActionParametersBase parentParameters = command.getParentParameters() == null ?
                command.getParentParameters(parentCommand) : command.getParentParameters();
        Guid parentCommandId =
                parentParameters == null ? Guid.Empty : parentParameters.getCommandId();

        if (ActionType.Unknown.equals(command.getParameters().getCommandType())) {
            command.getParameters().setCommandType(command.getActionType());
        }
        return new AsyncTask(AsyncTaskResultEnum.success,
                AsyncTaskStatusEnum.running,
                command.getUserId(),
                asyncTaskCreationInfo.getVdsmTaskId(),
                asyncTaskCreationInfo.getStepId(),
                asyncTaskCreationInfo.getStoragePoolID(),
                asyncTaskCreationInfo.getTaskType(),
                getParentCommandEntity(parentCommandId,
                        parentCommand,
                        parentParameters),
                getChildCommandEntity(command, parentCommand));
    }

    private CommandEntity getChildCommandEntity(CommandBase<?> command, ActionType parentCommand) {
        CommandEntity cmdEntity = coco.get().getCommandEntity(command.getCommandId());
        if (cmdEntity == null) {
            command.persistCommand(parentCommand, command.getCallback() != null);
        }
        return coco.get().getCommandEntity(command.getCommandId());
    }

    private CommandEntity getParentCommandEntity(Guid cmdId,
                                                 ActionType actionType,
                                                 ActionParametersBase parameters) {
        CommandEntity cmdEntity = coco.get().getCommandEntity(cmdId);
        if (cmdEntity == null) {
            cmdEntity = createCommandEntity(cmdId, actionType, parameters);
            if (!Guid.isNullOrEmpty(cmdId)) {
                cmdEntity.setCommandStatus(CommandStatus.ACTIVE);
                coco.get().persistCommand(cmdEntity);
            }
        }
        return cmdEntity;
    }

    public ActionReturnValue endAction(SPMTask task) {
        AsyncTask dbAsyncTask = task.getParameters().getDbAsyncTask();
        ActionType actionType = getEndActionType(dbAsyncTask);
        ActionParametersBase parameters = dbAsyncTask.getActionParameters();
        CommandBase<?> command = CommandHelper.buildCommand(actionType, parameters,
                coco.get().retrieveCommandContext(dbAsyncTask.getRootCommandId()).getExecutionContext(),
                coco.get().getCommandStatus(dbAsyncTask.getCommandId()));
        return new DecoratedCommand<>(command).endAction();
    }

    private ActionType getEndActionType(AsyncTask dbAsyncTask) {
        ActionType commandType = dbAsyncTask.getActionParameters().getCommandType();
        if (!ActionType.Unknown.equals(commandType)) {
            return commandType;
        }
        return dbAsyncTask.getActionType();
    }

    /**
     * @return The type of task that should be created for this command.
     * Commands that do not create async tasks should throw a
     * {@link UnsupportedOperationException}
     *
     */
    private AsyncTaskType internalGetTaskType(CommandBase<?> command) {
        return command.getAsyncTaskType();
    }
}
