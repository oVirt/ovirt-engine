package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;

public class TaskManagerUtil {

    public static final CommandCoordinatorImpl coco = new CommandCoordinatorImpl();

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
            CommandBase command,
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
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.concreteCreateTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand);
    }

    public static void cancelTasks(final CommandBase command) {
        coco.cancelTasks(command);
    }

    public static void revertTasks(final CommandBase command) {
        coco.revertTasks(command);
    }

    public static AsyncTasks getAsyncTask(
            Guid taskId,
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    public static AsyncTasks createAsyncTask(
            CommandBase command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return coco.createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    public static void logAndFailTaskOfCommandWithEmptyVdsmId(Guid taskId, String message) {
        getAsyncTaskManager().logAndFailTaskOfCommandWithEmptyVdsmId(taskId, message);
    }

    public static void logAndFailTaskOfCommandWithEmptyVdsmId(AsyncTasks task, String message) {
        getAsyncTaskManager().logAndFailTaskOfCommandWithEmptyVdsmId(task, message);
    }

    public static void removeTaskFromDbByTaskId(Guid taskId) {
        AsyncTaskManager.removeTaskFromDbByTaskId(taskId);
    }

    private static AsyncTaskManager getAsyncTaskManager() {
        return AsyncTaskManager.getInstance(coco);
    }

}
