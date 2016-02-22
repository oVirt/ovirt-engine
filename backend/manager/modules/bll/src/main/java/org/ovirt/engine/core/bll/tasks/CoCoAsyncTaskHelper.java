package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
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
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
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
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;

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
     * @param entitiesMap
     *            Map of entities that are associated with the task
     * @return Guid of the created task.
     */
    public Guid createTask(Guid taskId,
            CommandBase<?> command,
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
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
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
                ArrayList<AsyncTaskStatus> tasksStatuses = getAsyncTaskManager().pollTasks(
                        taskIdAsList);
                // call revert task only if ended successfully
                if (tasksStatuses.get(0).getTaskEndedSuccessfully()) {
                    getBackend().getResourceManager().runVdsCommand(
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
                    getAsyncTaskManager().cancelTasks(command.getReturnValue().getVdsmTaskIdList());
                } catch (Exception e) {
                    log.error("Failed to cancel tasks for command '{}'",
                            command.getClass().getName());
                }
            });
        }
    }

    public List<AsyncTask> getAllAsyncTasksFromDb(CommandCoordinator coco) {
        List<AsyncTask> asyncTasks = DbFacade.getInstance().getAsyncTaskDao().getAll();
        for (AsyncTask asyncTask : asyncTasks) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTasks;
    }

    private CommandEntity getCommandEntity(Guid cmdId) {
        CommandEntity cmdEntity = coco.getCommandEntity(cmdId);
        if (cmdEntity == null) {
            cmdEntity = coco.createCommandEntity(cmdId, VdcActionType.Unknown, new VdcActionParametersBase());
        }
        return cmdEntity;
    }

    /**
     * This method is always called from with in a transaction
     */
    public void saveAsyncTaskToDb(final AsyncTask asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            DbFacade.getInstance().getAsyncTaskDao().save(asyncTask);
            coco.persistCommand(asyncTask.getRootCmdEntity());
            coco.persistCommand(asyncTask.getChildCmdEntity());
            return null;
        });
    }

    public AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
        AsyncTask asyncTask = DbFacade.getInstance().getAsyncTaskDao().get(asyncTaskId);
        if (asyncTask != null) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTask;
    }

    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            AsyncTask asyncTask = DbFacade.getInstance().getAsyncTaskDao().get(taskId);
            int retVal = DbFacade.getInstance().getAsyncTaskDao().remove(taskId);
            if (shouldRemoveCommand(asyncTask)) {
                coco.removeCommand(asyncTask.getCommandId());
                if (!coco.hasCommandEntitiesWithRootCommandId(asyncTask.getRootCommandId())) {
                    coco.removeCommand(asyncTask.getRootCommandId());
                }
            }

            return retVal;
        });
    }

    private boolean shouldRemoveCommand(AsyncTask asyncTask) {
        if (asyncTask == null || Guid.isNullOrEmpty(asyncTask.getCommandId())) {
            return false;
        }

        CommandEntity cmdEntity = coco.getCommandEntity(asyncTask.getCommandId());
        CommandEntity parentEntity = null;
        if (cmdEntity != null) {
            parentEntity = coco.getCommandEntity(cmdEntity.getParentCommandId());
        }
        return cmdEntity != null && !cmdEntity.isCallbackEnabled() &&
                (parentEntity == null || !parentEntity.isCallbackEnabled());
    }

    public AsyncTask getByVdsmTaskId(Guid vdsmTaskId) {
        AsyncTask asyncTask = DbFacade.getInstance().getAsyncTaskDao().getByVdsmTaskId(vdsmTaskId);
        if (asyncTask != null) {
            asyncTask.setRootCmdEntity(getCommandEntity(asyncTask.getRootCommandId()));
            asyncTask.setChildCmdEntity(getCommandEntity(asyncTask.getCommandId()));
        }
        return asyncTask;
    }

    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            AsyncTask asyncTask = DbFacade.getInstance().getAsyncTaskDao().getByVdsmTaskId(vdsmTaskId);
            int retVal = DbFacade.getInstance().getAsyncTaskDao().removeByVdsmTaskId(vdsmTaskId);
            if (shouldRemoveCommand(asyncTask)) {
                coco.removeCommand(asyncTask.getCommandId());
                if (!coco.hasCommandEntitiesWithRootCommandId(asyncTask.getRootCommandId())) {
                    coco.removeCommand(asyncTask.getRootCommandId());
                }
            }

            return retVal;
        });
    }

    public void addOrUpdateTaskInDB(final AsyncTask asyncTask) {
        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            if (asyncTask.getChildCmdEntity().getRootCommandId() != null &&
                    !asyncTask.getChildCmdEntity().getRootCommandId().equals(asyncTask.getChildCmdEntity().getId())) {
                coco.persistCommand(asyncTask.getRootCmdEntity());
            }
            coco.persistCommand(asyncTask.getChildCmdEntity());
            DbFacade.getInstance().getAsyncTaskDao().saveOrUpdate(asyncTask);
            return null;
        });
    }

    public AsyncTask getAsyncTask(Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        AsyncTask asyncTask = null;
        if (!taskId.equals(Guid.Empty)) {
            asyncTask = getAsyncTaskFromDb(taskId);
        }
        if (asyncTask != null) {
            VdcActionParametersBase parentParameters = command.getParentParameters() == null ?
                    command.getParentParameters(parentCommand) : command.getParentParameters();
            Guid parentCommandId =
                    parentParameters == null ? Guid.Empty : parentParameters.getCommandId();
            if (VdcActionType.Unknown.equals(command.getParameters().getCommandType())) {
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
            VdcActionType parentCommand) {
        VdcActionParametersBase parentParameters = command.getParentParameters() == null ?
                command.getParentParameters(parentCommand) : command.getParentParameters();
        Guid parentCommandId =
                parentParameters == null ? Guid.Empty : parentParameters.getCommandId();

        if (VdcActionType.Unknown.equals(command.getParameters().getCommandType())) {
            command.getParameters().setCommandType(command.getActionType());
        }
        AsyncTask asyncTask = new AsyncTask(AsyncTaskResultEnum.success,
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
        return asyncTask;
    }

    private CommandEntity getChildCommandEntity(CommandBase<?> command, VdcActionType parentCommand) {
        CommandEntity cmdEntity = coco.getCommandEntity(command.getCommandId());
        if (cmdEntity == null) {
            command.persistCommand(parentCommand, command.getCallback() != null);
        }
        return coco.getCommandEntity(command.getCommandId());
    }

    private CommandEntity getParentCommandEntity(Guid cmdId,
                                                 VdcActionType actionType,
                                                 VdcActionParametersBase parameters) {
        CommandEntity cmdEntity = coco.getCommandEntity(cmdId);
        if (cmdEntity == null) {
            cmdEntity = coco.createCommandEntity(cmdId, actionType, parameters);
            if (!Guid.isNullOrEmpty(cmdId)) {
                cmdEntity.setCommandStatus(CommandStatus.ACTIVE);
                coco.persistCommand(cmdEntity);
            }
        }
        return cmdEntity;
    }

    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context) {
        AsyncTask dbAsyncTask = task.getParameters().getDbAsyncTask();
        VdcActionType actionType = getEndActionType(dbAsyncTask);
        VdcActionParametersBase parameters = dbAsyncTask.getActionParameters();
        CommandBase<?> command = CommandHelper.buildCommand(actionType, parameters, context, coco.getCommandStatus(dbAsyncTask.getCommandId()));
        return new DecoratedCommand<>(command).endAction();
    }

    private VdcActionType getEndActionType(AsyncTask dbAsyncTask) {
        VdcActionType commandType = dbAsyncTask.getActionParameters().getCommandType();
        if (!VdcActionType.Unknown.equals(commandType)) {
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
