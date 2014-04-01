package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Map;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
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
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class CommandCoordinatorImpl extends CommandCoordinator {

    private static final Log log = LogFactory.getLog(CommandCoordinator.class);
    private CommandsCache commandsCache;

    CommandCoordinatorImpl() {
        commandsCache = new CommandsCacheImpl();
    }

    public <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return CommandsFactory.createCommand(action, parameters);
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
        persistCommand(
                command.getCommandId(),
                task.getParameters().getDbAsyncTask().getRootCommandId(),
                task.getParameters().getDbAsyncTask().getActionParameters().getCommandType(),
                task.getParameters().getDbAsyncTask().getActionParameters(),
                command.getCommandStatus());
        getAsyncTaskManager().lockAndAddTaskToManager(task);
        Guid vdsmTaskId = task.getVdsmTaskId();
        ExecutionHandler.updateStepExternalId(taskStep, vdsmTaskId, ExternalSystemType.VDSM);
        return vdsmTaskId;

    }

    @Override
    public void persistCommand(Guid commandId,
                               Guid rootCommandId,
                               VdcActionType actionType,
                               VdcActionParametersBase params,
                               CommandStatus status) {
        commandsCache.put(
                commandId,
                rootCommandId,
                actionType,
                params,
                status);
    }

    @Override
    public CommandBase<?> retrieveCommand(Guid commandId) {
        CommandBase<?> command = null;
        CommandEntity cmdEntity = commandsCache.get(commandId);
        if (cmdEntity != null) {
            command = CommandsFactory.createCommand(cmdEntity.getCommandType(), cmdEntity.getActionParameters());
        }
        return command;
    }

    public void removeCommand(Guid commandId) {
        commandsCache.remove(commandId);
    }

    public void removeAllCommandsBeforeDate(DateTime cutoff) {
        commandsCache.removeAllCommandsBeforeDate(cutoff);
    }

    public void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status) {
        commandsCache.updateCommandStatus(commandId, taskType, status);
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

    public SPMAsyncTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return AsyncTaskFactory.construct(this, taskType, taskParameters, false);
    }

    public AsyncTasks getAsyncTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        AsyncTasks asyncTask = null;
        if (!taskId.equals(Guid.Empty)) {
            asyncTask = DbFacade.getInstance().getAsyncTaskDao().get(taskId);
        }
        if (asyncTask != null) {
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
        return new AsyncTasks(parentCommand,
                AsyncTaskResultEnum.success,
                AsyncTaskStatusEnum.running,
                asyncTaskCreationInfo.getVdsmTaskId(),
                parentParameters,
                command.getParameters(),
                asyncTaskCreationInfo.getStepId(),
                command.getCommandId(),
                parentParameters.getCommandId(),
                asyncTaskCreationInfo.getStoragePoolID(),
                asyncTaskCreationInfo.getTaskType());
    }

    /**
     * @return The type of task that should be created for this command.
     * Commands that do not create async tasks should throw a
     * {@link UnsupportedOperationException}
     *
     */
    public AsyncTaskType internalGetTaskType(CommandBase command) {
        if (command.hasTaskHandlers()) {
            if (command.getParameters().getExecutionReason() == VdcActionParametersBase.CommandExecutionReason.REGULAR_FLOW) {
                return command.getCurrentTaskHandler().getTaskType();
            }
            return command.getCurrentTaskHandler().getRevertTaskType();
        }
        return command.getAsyncTaskType();
    }

    public void cancelTasks(final CommandBase command) {
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

    @Override
    public ArrayList<AsyncTaskCreationInfo> getAllTasksInfo(Guid storagePoolID) {
        return (ArrayList<AsyncTaskCreationInfo>) runVdsCommand(VDSCommandType.SPMGetAllTasksInfo,
                new IrsBaseVDSCommandParameters(storagePoolID)).getReturnValue();
    }

    @Override
    public Map<Guid, AsyncTaskStatus>  getAllTasksStatuses(Guid storagePoolID) {
        return (Map<Guid, AsyncTaskStatus> ) runVdsCommand(VDSCommandType.SPMGetAllTasksStatuses,
                new IrsBaseVDSCommandParameters(storagePoolID)).getReturnValue();
    }

    @Override
    public void stopTask(Guid storagePoolID, Guid vdsmTaskID) {
        runVdsCommand(VDSCommandType.SPMStopTask,
                new SPMTaskGuidBaseVDSCommandParameters(storagePoolID, vdsmTaskID));
    }

    @Override
    public VDSReturnValue clearTask(Guid storagePoolID, Guid vdsmTaskID) {
        return runVdsCommand(VDSCommandType.SPMClearTask,
                new SPMTaskGuidBaseVDSCommandParameters(storagePoolID, vdsmTaskID));
    }

    private VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return Backend.getInstance().getResourceManager().RunVdsCommand(commandType, parameters);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo) {
        return AsyncTaskFactory.construct(this, creationInfo);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo, AsyncTasks asyncTask) {
        return AsyncTaskFactory.construct(this, creationInfo.getTaskType(), new AsyncTaskParameters(creationInfo, asyncTask), true);
    }

    @Override
    public SPMTask construct(AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams, boolean duringInit) {
        return AsyncTaskFactory.construct(this, taskType, asyncTaskParams, duringInit);
    }

    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context) {
        AsyncTasks dbAsyncTask = task.getParameters().getDbAsyncTask();
        VdcActionType actionType = getEndActionType(dbAsyncTask);
        VdcActionParametersBase parameters = dbAsyncTask.getActionParameters();
        CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters);
        command.setContext(new CommandContext(context));
        return new DecoratedCommand(command).endAction();
    }

    private VdcActionType getEndActionType(AsyncTasks dbAsyncTask) {
        VdcActionType commandType = dbAsyncTask.getActionParameters().getCommandType();
        if (!VdcActionType.Unknown.equals(commandType)) {
            return commandType;
        }
        return dbAsyncTask.getaction_type();
    }

    private AsyncTaskManager getAsyncTaskManager() {
        return AsyncTaskManager.getInstance(this);
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

}
