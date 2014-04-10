package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class CommandCoordinatorImpl extends CommandCoordinator {

    private static final Log log = LogFactory.getLog(CommandCoordinator.class);
    private final CommandsCache commandsCache;
    private final CoCoAsyncTaskHelper coCoAsyncTaskHelper;

    CommandCoordinatorImpl() {
        commandsCache = new CommandsCacheImpl();
        coCoAsyncTaskHelper = new CoCoAsyncTaskHelper(this);
    }

    public <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return CommandsFactory.createCommand(action, parameters);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity) {
        this.persistCommand(cmdEntity.getId(),
                cmdEntity.getRootCommandId(),
                cmdEntity.getCommandType(),
                cmdEntity.getActionParameters(),
                cmdEntity.getCommandStatus());
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
    public CommandEntity getCommandEntity(Guid commandId) {
        return commandsCache.get(commandId);
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

    public void removeCommand(final Guid commandId) {
        commandsCache.remove(commandId);
    }

    public void removeAllCommandsBeforeDate(final DateTime cutoff) {
        commandsCache.removeAllCommandsBeforeDate(cutoff);
    }

    public void updateCommandStatus(final Guid commandId, final AsyncTaskType taskType, final CommandStatus status) {
        commandsCache.updateCommandStatus(commandId, taskType, status);
    }

    public List<AsyncTasks> getAllAsyncTasksFromDb() {
        return coCoAsyncTaskHelper.getAllAsyncTasksFromDb(this);
    }

    public void saveAsyncTaskToDb(final AsyncTasks asyncTask) {
        coCoAsyncTaskHelper.saveAsyncTaskToDb(asyncTask);
    }

    public AsyncTasks getAsyncTaskFromDb(Guid asyncTaskId) {
        return coCoAsyncTaskHelper.getAsyncTaskFromDb(asyncTaskId);
    }

    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return coCoAsyncTaskHelper.removeTaskFromDbByTaskId(taskId);
    }

    public AsyncTasks getByVdsmTaskId(Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.getByVdsmTaskId(vdsmTaskId);
    }

    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.removeByVdsmTaskId(vdsmTaskId);
    }

    public void addOrUpdateTaskInDB(final AsyncTasks asyncTask) {
        coCoAsyncTaskHelper.addOrUpdateTaskInDB(asyncTask);
    }

    public SPMAsyncTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return coCoAsyncTaskHelper.createTask(taskType, taskParameters);
    }

    public AsyncTasks getAsyncTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    public AsyncTasks createAsyncTask(
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    public Guid createTask(Guid taskId,
                           CommandBase command,
                           AsyncTaskCreationInfo asyncTaskCreationInfo,
                           VdcActionType parentCommand,
                           String description,
                           Map<Guid, VdcObjectType> entitiesMap) {
        return coCoAsyncTaskHelper.createTask(taskId, command, asyncTaskCreationInfo, parentCommand, description, entitiesMap);

    }

    public SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.concreteCreateTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    public void cancelTasks(final CommandBase command) {
        coCoAsyncTaskHelper.cancelTasks(command, log);
    }

    public void revertTasks(CommandBase command) {
        coCoAsyncTaskHelper.revertTasks(command);
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
        return coCoAsyncTaskHelper.endAction(task, context);
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

}
