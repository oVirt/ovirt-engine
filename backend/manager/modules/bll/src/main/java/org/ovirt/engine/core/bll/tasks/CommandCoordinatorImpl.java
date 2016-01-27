package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
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
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCoordinatorImpl implements CommandCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CommandCoordinatorImpl.class);
    private final CoCoAsyncTaskHelper coCoAsyncTaskHelper;
    private final CommandExecutor cmdExecutor;
    private final CommandsRepository commandsRepository;

    CommandCoordinatorImpl() {
        coCoAsyncTaskHelper = new CoCoAsyncTaskHelper(this);
        commandsRepository = Injector.get(CommandsRepository.class);
        cmdExecutor = Injector.get(CommandExecutor.class);
    }

    public <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return CommandsFactory.createCommand(action, parameters);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        commandsRepository.persistCommand(cmdEntity, cmdContext);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity) {
        commandsRepository.persistCommand(cmdEntity);
    }

    @Override
    public void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities) {
        commandsRepository.persistCommandAssociatedEntities(cmdAssociatedEntities);
    }

    @Override
    public List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return commandsRepository.getCommandIdsByEntityId(entityId);
    }

    @Override
    public List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return commandsRepository.getCommandAssociatedEntities(cmdId);
    }

    /**
     * Executes the action using a Thread Pool. Used when the calling function
     * would like the execute the command with no delay
     */
    @Override
    public Future<VdcReturnValueBase> executeAsyncCommand(VdcActionType actionType,
                                                          VdcActionParametersBase parameters,
                                                          CommandContext cmdContext) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, cmdContext);
        CommandCallback callBack = command.getCallback();
        command.persistCommand(command.getParameters().getParentCommand(), cmdContext, callBack != null, false);
        if (callBack != null) {
            commandsRepository.addToCallbackMap(command.getCommandId(),
                    new CallbackTiming(callBack,
                            Config.<Integer> getValue(ConfigValues.AsyncCommandPollingLoopInSeconds)));
        }

        return cmdExecutor.executeAsyncCommand(command, cmdContext);
    }

    @Override
    public CommandEntity getCommandEntity(Guid commandId) {
        return commandsRepository.getCommandEntity(commandId);
    }

    @Override
    public CommandEntity createCommandEntity(Guid cmdId, VdcActionType actionType, VdcActionParametersBase params) {
        CommandEntity cmdEntity = new CommandEntity();
        cmdEntity.setId(cmdId);
        cmdEntity.setCommandType(actionType);
        cmdEntity.setCommandParameters(params);
        return cmdEntity;
    }

    @Override
    public CommandBase<?> retrieveCommand(Guid commandId) {
        return commandsRepository.retrieveCommand(commandId);
    }

    @Override
    public CommandStatus getCommandStatus(final Guid commandId) {
        return commandsRepository.getCommandStatus(commandId);
    }

    @Override
    public void removeAllCommandsInHierarchy(final Guid commandId) {
        for (Guid childCmdId : new ArrayList<>(getChildCommandIds(commandId))) {
            removeAllCommandsInHierarchy(childCmdId);
        }

        removeCommand(commandId);
    }

    @Override
    public void removeCommand(final Guid commandId) {
        commandsRepository.removeCommand(commandId);
    }

    @Override
    public void removeAllCommandsBeforeDate(final DateTime cutoff) {
        commandsRepository.removeAllCommandsBeforeDate(cutoff);
    }

    @Override
    public void updateCommandData(final Guid commandId, final Map<String, Serializable> data) {
        commandsRepository.updateCommandData(commandId, data);
    }

    @Override
    public void updateCommandStatus(final Guid commandId, final CommandStatus status) {
        commandsRepository.updateCommandStatus(commandId, status);
    }

    @Override
    public void updateCommandExecuted(Guid commandId) {
        commandsRepository.updateCommandExecuted(commandId);
    }

    @Override
    public boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId) {
        return commandsRepository.hasCommandEntitiesWithRootCommandId(rootCommandId);
    }

    @Override
    public List<Guid> getChildCommandIds(Guid cmdId) {
        return commandsRepository.getChildCommandIds(cmdId);
    }

    @Override
    public List<Guid> getChildCommandIds(Guid cmdId, VdcActionType childActionType, CommandStatus status) {
        List<Guid> childCmdIds = new ArrayList<>();
        for (Guid childCmdId : getChildCommandIds(cmdId)) {
            CommandEntity childCmdEntity = getCommandEntity(childCmdId);
            if (childCmdEntity != null &&
                    childCmdEntity.getCommandType().equals(childActionType) &&
                    (status == null || status.equals(childCmdEntity.getCommandStatus()))) {
                childCmdIds.add(childCmdId);
            }
        }
        return childCmdIds;
    }

    @Override
    public List<Guid> getCommandIdsBySessionSeqId(long engineSessionSeqId) {
        return commandsRepository.getCommandIdsBySessionSeqId(engineSessionSeqId);
    }

    @Override
    public List<CommandEntity> getChildCmdsByRootCmdId(Guid cmdId) {
        return commandsRepository.getChildCmdsByParentCmdId(cmdId);
    }

    @Override
    public List<AsyncTask> getAllAsyncTasksFromDb() {
        return coCoAsyncTaskHelper.getAllAsyncTasksFromDb(this);
    }

    @Override
    public void saveAsyncTaskToDb(final AsyncTask asyncTask) {
        coCoAsyncTaskHelper.saveAsyncTaskToDb(asyncTask);
    }

    @Override
    public AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
        return coCoAsyncTaskHelper.getAsyncTaskFromDb(asyncTaskId);
    }

    @Override
    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return coCoAsyncTaskHelper.removeTaskFromDbByTaskId(taskId);
    }

    @Override
    public AsyncTask getByVdsmTaskId(Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.getByVdsmTaskId(vdsmTaskId);
    }

    @Override
    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.removeByVdsmTaskId(vdsmTaskId);
    }

    @Override
    public void addOrUpdateTaskInDB(final AsyncTask asyncTask) {
        coCoAsyncTaskHelper.addOrUpdateTaskInDB(asyncTask);
    }

    public SPMAsyncTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return coCoAsyncTaskHelper.createTask(taskType, taskParameters);
    }

    @Override
    public AsyncTask getAsyncTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public Guid createTask(Guid taskId,
            CommandBase<?> command,
                           AsyncTaskCreationInfo asyncTaskCreationInfo,
                           VdcActionType parentCommand,
                           String description,
                           Map<Guid, VdcObjectType> entitiesMap) {
        return coCoAsyncTaskHelper.createTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                entitiesMap);

    }

    @Override
    public SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.concreteCreateTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public void cancelTasks(final CommandBase<?> command) {
        coCoAsyncTaskHelper.cancelTasks(command, log);
    }

    @Override
    public void revertTasks(CommandBase<?> command) {
        coCoAsyncTaskHelper.revertTasks(command);
    }

    @Override
    public ArrayList<AsyncTaskCreationInfo> getAllTasksInfo(Guid storagePoolID) {
        return (ArrayList<AsyncTaskCreationInfo>) runVdsCommand(VDSCommandType.SPMGetAllTasksInfo,
                new IrsBaseVDSCommandParameters(storagePoolID)).getReturnValue();
    }

    @Override
    public Map<Guid, AsyncTaskStatus> getAllTasksStatuses(Guid storagePoolID) {
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

    @Override
    public boolean doesCommandContainAsyncTask(Guid cmdId) {
        return AsyncTaskManager.getInstance().doesCommandContainAsyncTask(cmdId);
    }

    private VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return Backend.getInstance().getResourceManager().runVdsCommand(commandType, parameters);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo) {
        return AsyncTaskFactory.construct(this, creationInfo);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo, AsyncTask asyncTask) {
        return AsyncTaskFactory.construct(this,
                creationInfo.getTaskType(), new AsyncTaskParameters(creationInfo, asyncTask), true);
    }

    @Override
    public SPMTask construct(AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams, boolean duringInit) {
        return AsyncTaskFactory.construct(this, taskType, asyncTaskParams, duringInit);
    }

    @Override
    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context) {
        return coCoAsyncTaskHelper.endAction(task, context);
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    @Override
    public void subscribe(String eventKey, CommandEntity commandEntity) {
        commandsRepository.persistCommand(commandEntity);
        CoCoEventSubscriber subscriber = new CoCoEventSubscriber(eventKey, commandEntity, commandsRepository);
        getResourceManager().subscribe(subscriber);
        commandsRepository.addEventSubscription(commandEntity, subscriber);
    }

    private ResourceManager getResourceManager() {
        return Injector.get(ResourceManager.class);
    }

}
