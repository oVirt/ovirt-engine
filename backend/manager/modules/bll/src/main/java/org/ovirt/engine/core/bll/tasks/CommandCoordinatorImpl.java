package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
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

@Singleton
public class CommandCoordinatorImpl implements BackendService, CommandCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CommandCoordinatorImpl.class);

    @Inject
    private AsyncTaskFactory asyncTaskFactory;
    @Inject
    private CoCoAsyncTaskHelper coCoAsyncTaskHelper;
    @Inject
    private Instance<AsyncTaskManager> asyncTaskManager;
    @Inject
    private Instance<CommandsRepository> commandsRepositoryInstance;
    @Inject
    private Instance<CommandExecutor> commandExecutorInstance;
    @Inject
    private VDSBrokerFrontend resourceManager;

    public <P extends ActionParametersBase> CommandBase<P> createCommand(ActionType action, P parameters) {
        return CommandsFactory.createCommand(action, parameters);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        commandsRepositoryInstance.get().persistCommand(cmdEntity, cmdContext);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity) {
        commandsRepositoryInstance.get().persistCommand(cmdEntity);
    }

    @Override
    public void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities) {
        commandsRepositoryInstance.get().persistCommandAssociatedEntities(cmdAssociatedEntities);
    }

    @Override
    public List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return commandsRepositoryInstance.get().getCommandIdsByEntityId(entityId);
    }

    @Override
    public List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return commandsRepositoryInstance.get().getCommandAssociatedEntities(cmdId);
    }

    /**
     * Executes the action using a Thread Pool. Used when the calling function
     * would like the execute the command with no delay
     */
    @Override
    public Future<ActionReturnValue> executeAsyncCommand(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext cmdContext) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, cmdContext);
        CommandCallback callBack = command.getCallback();
        command.persistCommand(command.getParameters().getParentCommand(), cmdContext, callBack != null, false);
        if (callBack != null) {
            commandsRepositoryInstance.get().addToCallbackMap(command.getCommandId(),
                    new CallbackTiming(callBack,
                            Config.<Long>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds)));
        }

        return commandExecutorInstance.get().executeAsyncCommand(command, cmdContext);
    }

    @Override
    public CommandEntity getCommandEntity(Guid commandId) {
        return commandsRepositoryInstance.get().getCommandEntity(commandId);
    }

    @Override
    public CommandEntity createCommandEntity(Guid cmdId, ActionType actionType, ActionParametersBase params) {
        return coCoAsyncTaskHelper.createCommandEntity(cmdId, actionType, params);
    }

    @Override
    public CommandBase<?> retrieveCommand(Guid commandId) {
        return commandsRepositoryInstance.get().retrieveCommand(commandId);
    }

    @Override
    public CommandStatus getCommandStatus(final Guid commandId) {
        return commandsRepositoryInstance.get().getCommandStatus(commandId);
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
        commandsRepositoryInstance.get().removeCommand(commandId);
    }

    @Override
    public void removeAllCommandsBeforeDate(final DateTime cutoff) {
        commandsRepositoryInstance.get().removeAllCommandsBeforeDate(cutoff);
    }

    @Override
    public void updateCommandData(final Guid commandId, final Map<String, Serializable> data) {
        commandsRepositoryInstance.get().updateCommandData(commandId, data);
    }

    @Override
    public void updateCommandStatus(final Guid commandId, final CommandStatus status) {
        commandsRepositoryInstance.get().updateCommandStatus(commandId, status);
    }

    @Override
    public void updateCommandExecuted(Guid commandId) {
        commandsRepositoryInstance.get().updateCommandExecuted(commandId);
    }

    @Override
    public boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId) {
        return commandsRepositoryInstance.get().hasCommandEntitiesWithRootCommandId(rootCommandId);
    }

    @Override
    public List<Guid> getChildCommandIds(Guid cmdId) {
        return commandsRepositoryInstance.get().getChildCommandIds(cmdId);
    }

    @Override
    public List<Guid> getChildCommandIds(Guid cmdId, ActionType childActionType, CommandStatus status) {
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
        return commandsRepositoryInstance.get().getCommandIdsBySessionSeqId(engineSessionSeqId);
    }

    @Override
    public List<CommandEntity> getChildCmdsByRootCmdId(Guid cmdId) {
        return commandsRepositoryInstance.get().getChildCmdsByParentCmdId(cmdId);
    }

    @Override
    public List<AsyncTask> getAllAsyncTasksFromDb() {
        return coCoAsyncTaskHelper.getAllAsyncTasksFromDb();
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
            ActionType parentCommand) {
        return coCoAsyncTaskHelper.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return coCoAsyncTaskHelper.createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public Guid createTask(Guid taskId,
            CommandBase<?> command,
                           AsyncTaskCreationInfo asyncTaskCreationInfo,
                           ActionType parentCommand,
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
    public SPMAsyncTask concreteCreateTask(Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return coCoAsyncTaskHelper.concreteCreateTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand);
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
        return asyncTaskManager.get().doesCommandContainAsyncTask(cmdId);
    }

    private VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return resourceManager.runVdsCommand(commandType, parameters);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo) {
        return asyncTaskFactory.construct(creationInfo);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo, AsyncTask asyncTask) {
        return asyncTaskFactory.construct(creationInfo.getTaskType(),
                new AsyncTaskParameters(creationInfo, asyncTask),
                true);
    }

    @Override
    public SPMTask construct(AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams, boolean duringInit) {
        return asyncTaskFactory.construct(taskType, asyncTaskParams, duringInit);
    }

    @Override
    public ActionReturnValue endAction(SPMTask task) {
        return coCoAsyncTaskHelper.endAction(task);
    }

    @Override
    public CommandContext retrieveCommandContext(Guid cmdId) {
        return commandsRepositoryInstance.get().retrieveCommandContext(cmdId);
    }

    @Override
    public void subscribe(String eventKey, CommandEntity commandEntity) {
        commandsRepositoryInstance.get().persistCommand(commandEntity);
        CoCoEventSubscriber subscriber = new CoCoEventSubscriber(eventKey, commandEntity, commandsRepositoryInstance.get());
        getResourceManager().subscribe(subscriber);
        commandsRepositoryInstance.get().addEventSubscription(commandEntity, subscriber);
    }

    private ResourceManager getResourceManager() {
        return Injector.get(ResourceManager.class);
    }

}
