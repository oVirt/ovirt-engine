package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CoCoAsyncTaskHelper {

    private final CommandCoordinator coco;

    CoCoAsyncTaskHelper(CommandCoordinator coco) {
        this.coco = coco;
    }

    /**
     * Use this method in order to create task in the AsyncTaskManager in a safe way. If you use
     * this method within a certain command, make sure that the command implemented the
     * ConcreteCreateTask method.
     *
     * @param asyncTaskCreationInfo
     *            info to send to AsyncTaskManager when creating the task.
     * @param parentCommand
     *            VdcActionType of the command that its EndAction we want to invoke when tasks are finished.
     * @param entityType
     *            type of entities that are associated with the task
     * @param entityIds
     *            Ids of entities to be associated with task
     * @return Guid of the created task.
     */
    public Guid createTask(Guid taskId,
                           CommandBase command,
                           AsyncTaskCreationInfo asyncTaskCreationInfo,
                           VdcActionType parentCommand,
                           String description,
                           Map<Guid, VdcObjectType> entitiesMap) {
        Step taskStep =
                ExecutionHandler.addTaskStep(command.getExecutionContext(),
                        StepEnum.getStepNameByTaskType(asyncTaskCreationInfo.getTaskType()),
                        description);
        if (taskStep != null) {
            asyncTaskCreationInfo.setStepId(taskStep.getId());
        }
        SPMAsyncTask task = concreteCreateTask(taskId, command, asyncTaskCreationInfo, parentCommand);
        task.setEntitiesMap(entitiesMap);
        AsyncTaskUtils.addOrUpdateTaskInDB(task);
        getAsyncTaskManager().lockAndAddTaskToManager(task);
        Guid vdsmTaskId = task.getVdsmTaskId();
        ExecutionHandler.updateStepExternalId(taskStep, vdsmTaskId, ExternalSystemType.VDSM);
        return vdsmTaskId;

    }

    public SPMAsyncTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return AsyncTaskFactory.construct(coco, taskType, taskParameters, false);
    }

    /**
     * Create the {@link SPMAsyncTask} object to be run
     * @param taskId the id of the async task place holder in the database
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     * @return An {@link SPMAsyncTask} object representing the task to be run
     */
    public SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        AsyncTaskParameters p =
                new AsyncTaskParameters(asyncTaskCreationInfo,
                        getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand));
        p.setEntityInfo(command.getParameters().getEntityInfo());
        return createTask(internalGetTaskType(command), p);
    }

    public void revertTasks(CommandBase command) {
        if (command.getParameters().getVdsmTaskIds() != null) {
            // list to send to the pollTasks method
            ArrayList<Guid> taskIdAsList = new ArrayList<Guid>();

            for (Guid taskId : command.getParameters().getVdsmTaskIds()) {
                taskIdAsList.add(taskId);
                ArrayList<AsyncTaskStatus> tasksStatuses = getAsyncTaskManager().pollTasks(
                        taskIdAsList);
                // call revert task only if ended successfully
                if (tasksStatuses.get(0).getTaskEndedSuccessfully()) {
                    getBackend().getResourceManager().RunVdsCommand(
                            VDSCommandType.SPMRevertTask,
                            new SPMTaskGuidBaseVDSCommandParameters(
                                    command.getStoragePool().getId(), taskId));
                }
                taskIdAsList.clear();
            }
        }
    }

    public void cancelTasks(final CommandBase command, final Log log) {
        if (command.hasTasks()) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    log.infoFormat("Rollback for command: {0}.", command.getClass().getName());
                    try {
                        getAsyncTaskManager().cancelTasks(command.getReturnValue().getVdsmTaskIdList());
                    } catch (Exception e) {
                        log.errorFormat("Failed to cancel tasks for command: {0}.",
                                command.getClass().getName());
                    }
                }
            });
        }
    }

    public List<AsyncTasks> getAllAsyncTasksFromDb(CommandCoordinator coco) {
        List<AsyncTasks> asyncTasks = DbFacade.getInstance().getAsyncTaskDao().getAll();
        for (AsyncTasks asyncTask : asyncTasks) {
            map(coco.getCommandEntity(asyncTask.getCommandId()), asyncTask);
        }
        return asyncTasks;
    }

    private void map(CommandEntity cmdEntity, AsyncTasks asyncTask) {
        if (cmdEntity != null) {
            asyncTask.setTaskParameters(cmdEntity.getCommandParameters());
            asyncTask.setRootCommandId(cmdEntity.getRootCommandId());
            asyncTask.setCommandStatus(cmdEntity.getCommandStatus());
            asyncTask.setCommandType(cmdEntity.getCommandType());
            asyncTask.setCreatedAt(cmdEntity.getCreatedAt());
            asyncTask.setCallBackEnabled(cmdEntity.isCallBackEnabled());
        }
    }

    private CommandEntity map(AsyncTasks asyncTask, CommandEntity entity) {
        CommandStatus status = entity == null ? asyncTask.getCommandStatus() : entity.getCommandStatus();
        CommandEntity cmdEntity = entity == null ? new CommandEntity() : entity;
        cmdEntity.setId(asyncTask.getCommandId());
        cmdEntity.setCommandParameters(asyncTask.getTaskParameters());
        cmdEntity.setRootCommandId(asyncTask.getRootCommandId());
        cmdEntity.setCommandStatus(status);
        cmdEntity.setCommandType(asyncTask.getCommandType());
        cmdEntity.setCreatedAt(asyncTask.getCreatedAt());
        cmdEntity.setCallBackEnabled(asyncTask.isCallBackEnabled());
        return cmdEntity;
    }

    /**
     * This method is always called from with in a transaction
     * @param asyncTask
     */
    public void saveAsyncTaskToDb(final AsyncTasks asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getAsyncTaskDao().save(asyncTask);
                coco.persistCommand(map(asyncTask, coco.getCommandEntity(asyncTask.getCommandId())));
                return null;
            }
        });
    }

    public AsyncTasks getAsyncTaskFromDb(Guid asyncTaskId) {
        AsyncTasks asyncTask = DbFacade.getInstance().getAsyncTaskDao().get(asyncTaskId);
        if (asyncTask != null) {
            map(coco.getCommandEntity(asyncTask.getCommandId()), asyncTask);
        }
        return asyncTask;
    }

    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Integer>() {

            @Override
            public Integer runInTransaction() {
                AsyncTasks asyncTask = DbFacade.getInstance().getAsyncTaskDao().get(taskId);
                int retVal = DbFacade.getInstance().getAsyncTaskDao().remove(taskId);
                if (asyncTask != null && !Guid.isNullOrEmpty(asyncTask.getCommandId())) {
                    CommandEntity cmdEntity = coco.getCommandEntity(asyncTask.getCommandId());
                    if (cmdEntity != null && !cmdEntity.isCallBackEnabled()) {
                        coco.removeCommand(asyncTask.getCommandId());
                    }
                }
                return retVal;
            }
        });
    }

    public AsyncTasks getByVdsmTaskId(Guid vdsmTaskId) {
        AsyncTasks asyncTask = DbFacade.getInstance().getAsyncTaskDao().getByVdsmTaskId(vdsmTaskId);
        if (asyncTask != null) {
            map(coco.getCommandEntity(asyncTask.getCommandId()), asyncTask);
        }
        return asyncTask;
    }

    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Integer>() {

            @Override
            public Integer runInTransaction() {
                AsyncTasks asyncTask = DbFacade.getInstance().getAsyncTaskDao().getByVdsmTaskId(vdsmTaskId);
                int retVal = DbFacade.getInstance().getAsyncTaskDao().removeByVdsmTaskId(vdsmTaskId);
                if (asyncTask != null && !Guid.isNullOrEmpty(asyncTask.getCommandId())) {
                    CommandEntity cmdEntity = coco.getCommandEntity(asyncTask.getCommandId());
                    if (cmdEntity != null && !cmdEntity.isCallBackEnabled()) {
                        coco.removeCommand(asyncTask.getCommandId());
                    }
                }
                return retVal;
            }
        });
    }

    public void addOrUpdateTaskInDB(final AsyncTasks asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                CommandEntity cmdEntity = coco.getCommandEntity(asyncTask.getCommandId());
                if (asyncTask.getstatus() == AsyncTaskStatusEnum.finished) {
                    cmdEntity.setCommandStatus(CommandStatus.SUCCEEDED);
                }
                coco.persistCommand(map(asyncTask, cmdEntity));
                DbFacade.getInstance().getAsyncTaskDao().saveOrUpdate(asyncTask);
                return null;
            }
        });
    }

    public AsyncTasks getAsyncTask(Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        AsyncTasks asyncTask = null;
        if (!taskId.equals(Guid.Empty)) {
            asyncTask = getAsyncTaskFromDb(taskId);
        }
        if (asyncTask != null) {
            if (VdcActionType.Unknown.equals(command.getParameters().getCommandType())) {
                command.getParameters().setCommandType(command.getActionType());
            }
            VdcActionParametersBase parentParameters = command.getParentParameters(parentCommand);
            asyncTask.setaction_type(parentCommand);
            asyncTask.setVdsmTaskId(asyncTaskCreationInfo.getVdsmTaskId());
            asyncTask.setActionParameters(parentParameters);
            asyncTask.setTaskParameters(command.getParameters());
            asyncTask.setStepId(asyncTaskCreationInfo.getStepId());
            asyncTask.setCommandId(command.getCommandId());
            asyncTask.setRootCommandId(parentParameters.getCommandId());
            asyncTask.setStoragePoolId(asyncTaskCreationInfo.getStoragePoolID());
            asyncTask.setTaskType(asyncTaskCreationInfo.getTaskType());
            asyncTask.setCommandStatus(command.getCommandStatus());
            asyncTask.setCommandType(command.getParameters().getCommandType());
        } else {
            asyncTask = createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
        }
        return asyncTask;
    }

    public AsyncTasks createAsyncTask(
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        VdcActionParametersBase parentParameters = command.getParentParameters(parentCommand);
        if (VdcActionType.Unknown.equals(command.getParameters().getCommandType())) {
            command.getParameters().setCommandType(command.getActionType());
        }
        AsyncTasks asyncTask = new AsyncTasks(parentCommand,
                AsyncTaskResultEnum.success,
                AsyncTaskStatusEnum.running,
                asyncTaskCreationInfo.getVdsmTaskId(),
                parentParameters,
                command.getParameters(),
                asyncTaskCreationInfo.getStepId(),
                command.getCommandId(),
                command.getParameters().getParentParameters() == null ? Guid.Empty : command.getParameters().getParentParameters().getCommandId(),
                asyncTaskCreationInfo.getStoragePoolID(),
                asyncTaskCreationInfo.getTaskType(),
                command.getCommandStatus());
        return asyncTask;
    }

    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context) {
        AsyncTasks dbAsyncTask = task.getParameters().getDbAsyncTask();
        VdcActionType actionType = getEndActionType(dbAsyncTask);
        VdcActionParametersBase parameters = dbAsyncTask.getActionParameters();
        CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters);
        command.getContext().withExecutionContext(context);
        return new DecoratedCommand(command).endAction();
    }

    private VdcActionType getEndActionType(AsyncTasks dbAsyncTask) {
        VdcActionType commandType = dbAsyncTask.getActionParameters().getCommandType();
        if (!VdcActionType.Unknown.equals(commandType)) {
            return commandType;
        }
        return dbAsyncTask.getaction_type();
    }

    /**
     * @return The type of task that should be created for this command.
     * Commands that do not create async tasks should throw a
     * {@link UnsupportedOperationException}
     *
     */
    private AsyncTaskType internalGetTaskType(CommandBase command) {
        if (command.hasTaskHandlers()) {
            if (command.getParameters().getExecutionReason() == VdcActionParametersBase.CommandExecutionReason.REGULAR_FLOW) {
                return command.getCurrentTaskHandler().getTaskType();
            }
            return command.getCurrentTaskHandler().getRevertTaskType();
        }
        return command.getAsyncTaskType();
    }

    private AsyncTaskManager getAsyncTaskManager() {
        return AsyncTaskManager.getInstance();
    }

    private BackendInternal getBackend() {
        return Backend.getInstance();
    }
}
