package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandContextsCache;
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
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCoordinatorImpl extends CommandCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CommandCoordinator.class);
    private final CommandsCache commandsCache;
    private final CommandContextsCache contextsCache;
    private final CoCoAsyncTaskHelper coCoAsyncTaskHelper;
    private final CommandExecutor cmdExecutor;
    private Object LOCK = new Object();
    private volatile boolean childHierarchyInitialized;
    private ConcurrentHashMap<Guid, List<Guid>> childHierarchy = new ConcurrentHashMap<>();

    CommandCoordinatorImpl() {
        commandsCache = new CommandsCacheImpl();
        contextsCache = new CommandContextsCacheImpl(commandsCache);
        coCoAsyncTaskHelper = new CoCoAsyncTaskHelper(this);
        cmdExecutor = new CommandExecutor(this);
    }

    public <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return CommandsFactory.createCommand(action, parameters);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        initChildHierarchy();
        if (Guid.isNullOrEmpty(cmdEntity.getId())) {
            return;
        }
        persistCommand(cmdEntity);
        saveCommandContext(cmdEntity.getId(), cmdContext);
    }

    @Override
    public void persistCommand(CommandEntity cmdEntity) {
        if (Guid.isNullOrEmpty(cmdEntity.getId())) {
            return;
        }
        CommandEntity existingCmdEntity = commandsCache.get(cmdEntity.getId());
        if (existingCmdEntity != null) {
            cmdEntity.setExecuted(existingCmdEntity.isExecuted());
            cmdEntity.setCallBackNotified(existingCmdEntity.isCallBackNotified());
        }
        commandsCache.put(cmdEntity);
        // check if callback is enabled or if parent command has callback enabled
        if (cmdEntity.isCallBackEnabled() ||
                (!Guid.isNullOrEmpty(cmdEntity.getRootCommandId()) &&
                        commandsCache.get(cmdEntity.getRootCommandId()) != null &&
                        commandsCache.get(cmdEntity.getRootCommandId()).isCallBackEnabled()
                )) {
            buildCmdHierarchy(cmdEntity);
            if (!cmdEntity.isCallBackNotified()) {
                cmdExecutor.addToCallBackMap(cmdEntity);
            }
        }
    }

    void saveCommandContext(Guid cmdId, CommandContext cmdContext) {
        if (cmdContext != null) {
            contextsCache.put(cmdId, cmdContext);
        }
    }

    /**
     * Executes the action using a Thread Pool. Used when the calling function
     * would like the execute the command with no delay
     */
    @Override
    public Future<VdcReturnValueBase> executeAsyncCommand(VdcActionType actionType,
                                                          VdcActionParametersBase parameters,
                                                          CommandContext cmdContext) {
        return cmdExecutor.executeAsyncCommand(actionType, parameters, cmdContext);
    }

    @Override
    public CommandEntity getCommandEntity(Guid commandId) {
        return Guid.isNullOrEmpty(commandId) ? null : commandsCache.get(commandId);
    }

    @Override
    public CommandEntity createCommandEntity(Guid cmdId, VdcActionType actionType, VdcActionParametersBase params) {
        CommandEntity cmdEntity = new CommandEntity();
        cmdEntity.setId(cmdId);
        cmdEntity.setCommandType(actionType);
        cmdEntity.setCommandParameters(params);
        return cmdEntity;
    }

    public List<CommandEntity> getCommandsWithCallBackEnabled() {
        List<CommandEntity> cmdEntities = new ArrayList<>();
        CommandEntity cmdEntity;
        for (Guid cmdId : commandsCache.keySet()) {
            cmdEntity = commandsCache.get(cmdId);
            if (commandsCache.get(cmdId).isCallBackEnabled()) {
                cmdEntities.add(cmdEntity);
            }
        }
        return cmdEntities;
    }

    @Override
    public CommandBase<?> retrieveCommand(Guid commandId) {
        return buildCommand(commandsCache.get(commandId), contextsCache.get(commandId));
    }

    private CommandBase<?> buildCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        CommandBase<?> command = null;
        if (cmdEntity != null) {
            if (cmdContext == null) {
                cmdContext = new CommandContext(new EngineContext()).withExecutionContext(new ExecutionContext());
            }
            if (CommandsFactory.hasConstructor(cmdEntity.getCommandType(), cmdEntity.getCommandParameters(), cmdContext)) {
                command = CommandsFactory.createCommand(cmdEntity.getCommandType(), cmdEntity.getCommandParameters(), cmdContext);
            } else {
                command = CommandsFactory.createCommand(cmdEntity.getCommandType(), cmdEntity.getCommandParameters());
            }

            command.setCommandStatus(cmdEntity.getCommandStatus(), false);
            if (!Guid.isNullOrEmpty(cmdEntity.getRootCommandId()) &&
                    ! cmdEntity.getRootCommandId().equals(cmdEntity.getId()) &&
                    command.getParameters().getParentParameters() == null) {
                CommandBase<?> parentCommand = retrieveCommand(cmdEntity.getRootCommandId());
                if (parentCommand != null) {
                    command.getParameters().setParentParameters(parentCommand.getParameters());
                }
            }
        }
        return command;
    }

    public CommandStatus getCommandStatus(final Guid commandId) {
        CommandEntity cmdEntity = commandsCache.get(commandId);
        if (cmdEntity != null) {
            return cmdEntity.getCommandStatus();
        }
        return CommandStatus.UNKNOWN;
    }

    public void removeAllCommandsInHierarchy(final Guid commandId) {
        for (Guid childCmdId : new ArrayList<Guid>(getChildCommandIds(commandId))) {
            removeAllCommandsInHierarchy(childCmdId);
        }
        removeCommand(commandId);
    }

    public void removeCommand(final Guid commandId) {
        commandsCache.remove(commandId);
        contextsCache.remove(commandId);
        updateCmdHierarchy(commandId);
    }

    public void removeAllCommandsBeforeDate(final DateTime cutoff) {
        commandsCache.removeAllCommandsBeforeDate(cutoff);
        synchronized(LOCK) {
            childHierarchyInitialized = false;
        }
    }

    public void updateCommandStatus(final Guid commandId, final CommandStatus status) {
        commandsCache.updateCommandStatus(commandId, status);
    }

    public void updateCommandExecuted(Guid commandId) {
        commandsCache.updateCommandExecuted(commandId);
    }

    public void updateCallBackNotified(final Guid commandId) {
        commandsCache.updateCallBackNotified(commandId);
    }

    public boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId) {
        CommandEntity cmdEntity;
        for (Guid cmdId : commandsCache.keySet()) {
            cmdEntity = commandsCache.get(cmdId);
            if (cmdEntity != null && !Guid.isNullOrEmpty(cmdEntity.getRootCommandId()) &&
                    !cmdEntity.getRootCommandId().equals(cmdId) &&
                    cmdEntity.getRootCommandId().equals(rootCommandId)) {
                return true;
            }
        }
        return false;
    }

    public List<Guid> getChildCommandIds(Guid cmdId) {
        initChildHierarchy();
        if (childHierarchy.containsKey(cmdId)) {
            return childHierarchy.get(cmdId);
        }
        return Collections.emptyList();
    }

    public List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId) {
        return commandsCache.getChildCmdsByParentCmdId(cmdId);
    }

    private void initChildHierarchy() {
        if (!childHierarchyInitialized) {
            synchronized(LOCK) {
                if (!childHierarchyInitialized) {
                    childHierarchy.clear();
                    for (CommandEntity cmd : getCommandsWithCallBackEnabled()) {
                        buildCmdHierarchy(cmd);
                    }
                }
                childHierarchyInitialized = true;
            }
        }
    }

    private void buildCmdHierarchy(CommandEntity cmdEntity) {
        if (!Guid.isNullOrEmpty(cmdEntity.getRootCommandId()) && !cmdEntity.getId().equals(cmdEntity.getRootCommandId())) {
            childHierarchy.putIfAbsent(cmdEntity.getRootCommandId(), new ArrayList<Guid>());
            if (!childHierarchy.get(cmdEntity.getRootCommandId()).contains(cmdEntity.getId())) {
                childHierarchy.get(cmdEntity.getRootCommandId()).add(cmdEntity.getId());
            }
        }
    }

    private void updateCmdHierarchy(Guid cmdId) {
        for (List<Guid> childIds : childHierarchy.values()) {
            if (childIds.contains(cmdId)) {
                childIds.remove(cmdId);
                break;
            }
        }
        if (childHierarchy.containsKey(cmdId) && childHierarchy.get(cmdId).size() == 0) {
            childHierarchy.remove(cmdId);
        }
    }

    public List<AsyncTask> getAllAsyncTasksFromDb() {
        return coCoAsyncTaskHelper.getAllAsyncTasksFromDb(this);
    }

    public void saveAsyncTaskToDb(final AsyncTask asyncTask) {
        coCoAsyncTaskHelper.saveAsyncTaskToDb(asyncTask);
    }

    public AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
        return coCoAsyncTaskHelper.getAsyncTaskFromDb(asyncTaskId);
    }

    public int removeTaskFromDbByTaskId(final Guid taskId) throws RuntimeException {
        return coCoAsyncTaskHelper.removeTaskFromDbByTaskId(taskId);
    }

    public AsyncTask getByVdsmTaskId(Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.getByVdsmTaskId(vdsmTaskId);
    }

    public int removeByVdsmTaskId(final Guid vdsmTaskId) {
        return coCoAsyncTaskHelper.removeByVdsmTaskId(vdsmTaskId);
    }

    public void addOrUpdateTaskInDB(final AsyncTask asyncTask) {
        coCoAsyncTaskHelper.addOrUpdateTaskInDB(asyncTask);
    }

    public SPMAsyncTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return coCoAsyncTaskHelper.createTask(taskType, taskParameters);
    }

    public AsyncTask getAsyncTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coCoAsyncTaskHelper.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    public AsyncTask createAsyncTask(
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

    @Override
    public boolean doesCommandContainAsyncTask(Guid cmdId) {
        return AsyncTaskManager.getInstance().doesCommandContainAsyncTask(cmdId);
    }

    private VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return Backend.getInstance().getResourceManager().RunVdsCommand(commandType, parameters);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo) {
        return AsyncTaskFactory.construct(this, creationInfo);
    }

    @Override
    public SPMTask construct(AsyncTaskCreationInfo creationInfo, AsyncTask asyncTask) {
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
