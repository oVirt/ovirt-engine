package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

public class CommandCoordinatorUtil {

    public static final CommandCoordinator coco = new CommandCoordinatorImpl();

    public static void startPollingTask(Guid taskID) {
        getAsyncTaskManager().startPollingTask(taskID);
    }

    public static void addStoragePoolExistingTasks(StoragePool sp) {
        getAsyncTaskManager().addStoragePoolExistingTasks(sp);
    }

    public static boolean hasTasksForEntityIdAndAction(Guid id, VdcActionType type) {
        return getAsyncTaskManager().hasTasksForEntityIdAndAction(id, type);
    }

    public static boolean hasTasksByStoragePoolId(Guid storagePoolID) {
        return getAsyncTaskManager().hasTasksByStoragePoolId(storagePoolID);
    }

    public static void initAsyncTaskManager() {
        getAsyncTaskManager().initAsyncTaskManager();
    }

    public static boolean entityHasTasks(Guid id) {
        return getAsyncTaskManager().entityHasTasks(id);
    }

    public static ArrayList<AsyncTaskStatus> pollTasks(java.util.ArrayList<Guid> taskIdList) {
        return getAsyncTaskManager().pollTasks(taskIdList);
    }

    public static Guid createTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap) {
        return coco.createTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                entitiesMap);
    }

    public static SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.concreteCreateTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand);
    }

    public static void cancelTasks(final CommandBase<?> command) {
        coco.cancelTasks(command);
    }

    public static void revertTasks(final CommandBase<?> command) {
        coco.revertTasks(command);
    }

    public static AsyncTask getAsyncTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    public static AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    public static void logAndFailTaskOfCommandWithEmptyVdsmId(Guid taskId, String message) {
        getAsyncTaskManager().logAndFailTaskOfCommandWithEmptyVdsmId(taskId, message);
    }

    public static void logAndFailTaskOfCommandWithEmptyVdsmId(AsyncTask task, String message) {
        getAsyncTaskManager().logAndFailPartiallySubmittedTaskOfCommand(task, message);
    }

    public static Collection<Guid> getUserIdsForVdsmTaskIds(ArrayList<Guid> tasksIDs) {
        return getAsyncTaskManager().getUserIdsForVdsmTaskIds(tasksIDs);
    }

    public static void removeTaskFromDbByTaskId(Guid taskId) {
        AsyncTaskManager.removeTaskFromDbByTaskId(taskId);
    }

    public static AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
         return coco.getAsyncTaskFromDb(asyncTaskId);
    }

    public static void saveAsyncTaskToDb(AsyncTask asyncTask) {
        coco.saveAsyncTaskToDb(asyncTask);
    }

    public static int callRemoveTaskFromDbByTaskId(Guid taskId) {
        return coco.removeTaskFromDbByTaskId(taskId);
    }

    public static void addOrUpdateTaskInDB(AsyncTask asyncTask) {
        coco.addOrUpdateTaskInDB(asyncTask);
    }

    public static void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        coco.persistCommand(cmdEntity, cmdContext);
    }

    public static List<Guid> getChildCommandIds(Guid commandId) {
        return coco.getChildCommandIds(commandId);
    }

    public static CommandEntity getCommandEntity(Guid commandId) {
        return coco.getCommandEntity(commandId);
    }

    @SuppressWarnings("unchecked")
    public static <C extends CommandBase<?>> C retrieveCommand(Guid commandId) {
        return (C) coco.retrieveCommand(commandId);
    }

    public static void removeCommand(Guid commandId) {
        coco.removeCommand(commandId);
    }

    public static void removeAllCommandsInHierarchy(Guid commandId) {
        coco.removeAllCommandsInHierarchy(commandId);
    }

    public static void removeAllCommandsBeforeDate(DateTime cutoff) {
        coco.removeAllCommandsBeforeDate(cutoff);
    }

    public static CommandStatus getCommandStatus(Guid commandId) {
        return coco.getCommandStatus(commandId);
    }

    public static void updateCommandStatus(Guid commandId, CommandStatus status) {
         coco.updateCommandStatus(commandId, status);
    }

    public static CommandExecutionStatus getCommandExecutionStatus(Guid commandId) {
        CommandEntity cmdEntity = coco.getCommandEntity(commandId);
        return cmdEntity == null ? CommandExecutionStatus.UNKNOWN :
                cmdEntity.isExecuted() ? CommandExecutionStatus.EXECUTED : CommandExecutionStatus.NOT_EXECUTED;
    }

    public static void updateCommandExecuted(Guid commandId) {
        coco.updateCommandExecuted(commandId);
    }

    public static Future<VdcReturnValueBase> executeAsyncCommand(VdcActionType actionType,
                                                                 VdcActionParametersBase parameters,
                                                                 CommandContext cmdContext,
                                                                 SubjectEntity... subjectEntities) {
        return coco.executeAsyncCommand(actionType, parameters, cmdContext, subjectEntities);
    }

    public static List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return coco.getCommandIdsByEntityId(entityId);
    }

    public static List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return coco.getCommandAssociatedEntities(cmdId);
    }

    public static VdcReturnValueBase getCommandReturnValue(Guid cmdId) {
        CommandEntity cmdEnity = coco.getCommandEntity(cmdId);
        return cmdEnity == null ? null : cmdEnity.getReturnValue();
    }

    private static AsyncTaskManager getAsyncTaskManager() {
        return AsyncTaskManager.getInstance(coco);
    }

}
