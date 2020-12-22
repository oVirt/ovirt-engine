package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

@Singleton
public class CommandCoordinatorUtil implements BackendService {

    @Inject
    private Instance<CommandCoordinator> coco;

    @Inject
    private Instance<AsyncTaskManager> asyncTaskManager;

    /**
     * Start polling the task identified by the vdsm task id
     * @param taskID The vdsm task id
     */
    public void startPollingTask(Guid taskID) {
        asyncTaskManager.get().startPollingTask(taskID);
    }

    /**
     * Retrieves from the specified storage pool the tasks that exist on it and
     * adds them to the async task manager.
     *
     * @param sp the storage pool to retrieve running tasks from
     */
    public void addStoragePoolExistingTasks(StoragePool sp) {
        asyncTaskManager.get().addStoragePoolExistingTasks(sp);
    }

    /**
     * Checks if there are tasks of the specified type existing on the entity
     * @param id The entity id
     * @param type The action type
     */
    public boolean hasTasksForEntityIdAndAction(Guid id, ActionType type) {
        return asyncTaskManager.get().hasTasksForEntityIdAndAction(id, type);
    }

    /**
     * Checks if there are tasks existing on the storage pool
     * @param storagePoolId the id of the storage pool
     */
    public boolean hasTasksByStoragePoolId(Guid storagePoolId) {
        return asyncTaskManager.get().hasTasksByStoragePoolId(storagePoolId);
    }

    /**
     * Checks if there are tasks on the entity
     * @param id The entity id
     */
    public boolean entityHasTasks(Guid id) {
        return asyncTaskManager.get().entityHasTasks(id);
    }

    /**
     * Poll vdsm for the task status and update the task, returning a list of task statuses.
     * @param taskIdList The list of task ids
     * @return List of async task status
     */
    public ArrayList<AsyncTaskStatus> pollTasks(java.util.ArrayList<Guid> taskIdList) {
        return asyncTaskManager.get().pollTasks(taskIdList);
    }

    /**
     * Poll vdsm for the task status and update the task, returning a map from task id to task status.
     * @param taskIds The set of task ids
     * @return Map from task id to its status.
     */
    public Map<Guid, AsyncTaskStatus> pollTasks(Set<Guid> taskIds) {
        return asyncTaskManager.get().pollTasksWithStatus(taskIds);
    }

    /**
     * Create an Async Task.
     * @param taskId the id of the async task place holder in the database
     * @param command the command object creating the task
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     * @param description A message which describes the task
     * @param entitiesMap map of entities
     */
    public Guid createTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap) {
        return coco.get().createTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                entitiesMap);
    }

    /**
     * Create the {@link SPMAsyncTask} object to be run
     * @param taskId the id of the async task place holder in the database
     * @param command the command object creating the task
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     */
    public SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return coco.get().concreteCreateTask(taskId,
                command,
                asyncTaskCreationInfo,
                parentCommand);
    }

    /**
     * Stops all tasks, and set them to polling state, for clearing them up later.
     * @param command The command whose vdsm tasks need to be cancelled
     */
    public void cancelTasks(final CommandBase<?> command) {
        coco.get().cancelTasks(command);
    }

    /**
     * Revert the vdsm tasks associated with the command
     * @param command The command whose vdsm tasks need to be reverted
     */
    public void revertTasks(final CommandBase<?> command) {
        coco.get().revertTasks(command);
    }

    /**
     * Retrieve the async task by task id, create one if it does not exist.
     * @param taskId the id of the async task place holder in the database
     * @param command the command object retrieving or creating the task
     * @param asyncTaskCreationInfo Info on how to create the task if one does not exist
     * @param parentCommand The type of command issuing the task
     */
    public AsyncTask getAsyncTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return coco.get().getAsyncTask(taskId, command, asyncTaskCreationInfo, parentCommand);
    }

    /**
     * Create Async Task to be run
     * @param command the command object creating the task
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     */
    public AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return coco.get().createAsyncTask(command, asyncTaskCreationInfo, parentCommand);
    }

    /**
     * Fail the command associated with the task identified by the task id that has empty vdsm id and log the failure
     * with the specified message.
     * @param taskId the id of the async task in the database
     * @param message the message to be logged for the failure
     */
    public void logAndFailTaskOfCommandWithEmptyVdsmId(Guid taskId, String message) {
        asyncTaskManager.get().logAndFailTaskOfCommandWithEmptyVdsmId(taskId, message);
    }

    /**
     * Get the ids of the users executing the vdsm tasks.
     * @param tasksIDs The vdsm task ids being executed
     * @return The collection of user ids
     */
    public Collection<Guid> getUserIdsForVdsmTaskIds(ArrayList<Guid> tasksIDs) {
        return asyncTaskManager.get().getUserIdsForVdsmTaskIds(tasksIDs);
    }

    /**
     * Remove the async task identified the task id from the database.
     * @param taskId The task id of the async task in the database
     */
    public void removeTaskFromDbByTaskId(Guid taskId) {
        asyncTaskManager.get().removeTaskFromDbByTaskId(taskId);
    }

    /**
     * Get the async task from the database identified by the asyncTaskId
     * @return The async task to be saved
     */
    public AsyncTask getAsyncTaskFromDb(Guid asyncTaskId) {
         return coco.get().getAsyncTaskFromDb(asyncTaskId);
    }

    /**
     * Save the async task in the database
     * @param asyncTask the async task to be saved in to the database
     */
    public void saveAsyncTaskToDb(AsyncTask asyncTask) {
        coco.get().saveAsyncTaskToDb(asyncTask);
    }

    /**
     * Persist the command entity in the database
     * @param cmdEntity The command entity to be persisted
     * @param cmdContext The CommandContext object associated with the command being persisted
     */
    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        coco.get().persistCommand(cmdEntity, cmdContext);
    }

    /**
     * Persist the command related entities in the database
     */
    public void persistCommandAssociatedEntities(Guid cmdId, Collection<SubjectEntity> subjectEntities) {
        coco.get().persistCommandAssociatedEntities(buildCommandAssociatedEntities(cmdId, subjectEntities));
    }

    private Collection<CommandAssociatedEntity> buildCommandAssociatedEntities(Guid cmdId,
            Collection<SubjectEntity> subjectEntities) {
        if (subjectEntities.isEmpty()) {
            return Collections.emptySet();
        }

        return subjectEntities.stream().map(subjectEntity -> new CommandAssociatedEntity(cmdId,
                subjectEntity.getEntityType(),
                subjectEntity.getEntityId()))
                .collect(Collectors.toSet());
    }

    /**
     * Return the child command ids for the parent command identified by commandId
     * @param commandId The id of the parent command
     * @return The list of child command ids
     */
    public List<Guid> getChildCommandIds(Guid commandId) {
        return coco.get().getChildCommandIds(commandId);
    }

    /**
     * Return the child command ids for the parent command identified by commandId with the given action type and
     * status.
     * @param commandId The id of the parent command
     * @param childActionType The action type of the child command
     * @param status The status of the child command, can be null
     */
    public List<Guid> getChildCommandIds(Guid commandId, ActionType childActionType, CommandStatus status) {
        return coco.get().getChildCommandIds(commandId, childActionType, status);
    }

    /**
     * Return the command ids being executed by the user identified by engine session seq id.
     * @param engineSessionSeqId The id of the user's engine session
     */
    public List<Guid> getCommandIdsBySessionSeqId(long engineSessionSeqId) {
        return coco.get().getCommandIdsBySessionSeqId(engineSessionSeqId);
    }

    /**
     * Get the command entity for the command identified by the commandId
     * @param commandId The id of the command
     * @return The command entity for the command id
     */
    public CommandEntity getCommandEntity(Guid commandId) {
        return coco.get().getCommandEntity(commandId);
    }

    /**
     * Get the command object for the command identified by command id.
     * @param commandId The id of the command
     * @return The command
     */
    @SuppressWarnings("unchecked")
    public <C extends CommandBase<?>> C retrieveCommand(Guid commandId) {
        return (C) coco.get().retrieveCommand(commandId);
    }

    /**
     * Remove the command entity for the command identified by command id
     * @param commandId The id of the command
     */
    public void removeCommand(Guid commandId) {
        coco.get().removeCommand(commandId);
    }

    /**
     * Remove the command entities for the command and the command's child commands from the database
     * @param commandId The id of the command
     */
    public void removeAllCommandsInHierarchy(Guid commandId) {
        coco.get().removeAllCommandsInHierarchy(commandId);
    }

    /**
     * Remove all command entities whose creation date is before the cutoff
     * @param cutoff The cutoff date
     */
    public void removeAllCommandsBeforeDate(DateTime cutoff) {
        coco.get().removeAllCommandsBeforeDate(cutoff);
    }

    /**
     * Get the status of command identified by command id
     * @param commandId The id of the command
     * @return The status of the command
     */
    public CommandStatus getCommandStatus(Guid commandId) {
        return coco.get().getCommandStatus(commandId);
    }

    /**
     * Update the status of command identified by command id
     * @param commandId The id of the command
     * @param status The new status of the command
     */
    public void updateCommandStatus(Guid commandId, CommandStatus status) {
         coco.get().updateCommandStatus(commandId, status);
    }

    /**
     * Get the async task command execution status
     * @param commandId The id of the command
     * @return The async task command execution status
     */
    public CommandExecutionStatus getCommandExecutionStatus(Guid commandId) {
        CommandEntity cmdEntity = coco.get().getCommandEntity(commandId);
        return cmdEntity == null ? CommandExecutionStatus.UNKNOWN :
                cmdEntity.isExecuted() ? CommandExecutionStatus.EXECUTED : CommandExecutionStatus.NOT_EXECUTED;
    }

    /**
     * Returns the command entity's data for the command identified by the command id.
     * @param commandId The id of the command
     */
    public Map<String, Serializable> getCommandData(Guid commandId) {
        CommandEntity cmdEntity = coco.get().getCommandEntity(commandId);
        return cmdEntity == null ? new HashMap<>() : cmdEntity.getData();
    }

    /**
     * Set the command entity's data for the command identified by the command id.
     * @param commandId The id of the command
     */
    public void updateCommandData(Guid commandId, Map<String, Serializable> data) {
        coco.get().updateCommandData(commandId, data);
    }

    /**
     * Set the command entity's executed status for the command identified by the command id.
     * @param commandId The id of the command
     */
    public void updateCommandExecuted(Guid commandId) {
        coco.get().updateCommandExecuted(commandId);
    }

    /**
     * Submit the command for asynchronous execution to the Command Executor thread pool.
     * @param actionType The action type of the command
     * @param parameters The parameters for the command
     * @param cmdContext The command context for the command
     * @return The future object for the command submitted to the thread pool
     */
    public Future<ActionReturnValue> executeAsyncCommand(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext cmdContext) {
        return coco.get().executeAsyncCommand(actionType, parameters, cmdContext);
    }

    /**
     * Get the ids of the commands which are associated with the entity.
     * @param entityId The id of the entity
     * @return The list of command ids
     */
    public List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return coco.get().getCommandIdsByEntityId(entityId);
    }

    /**
     * Get the associated entities for the command
     * @param cmdId The id of the entity
     * @return The list of associated entities
     */
    public List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return coco.get().getCommandAssociatedEntities(cmdId);
    }

    /**
     * Get the return value for the command identified by the command id.
     * @param cmdId The id of the command
     * @return The return value for the command
     */
    public ActionReturnValue getCommandReturnValue(Guid cmdId) {
        CommandEntity cmdEnity = coco.get().getCommandEntity(cmdId);
        return cmdEnity == null ? null : cmdEnity.getReturnValue();
    }

    /**
     * Subscribes the given command for an event by its given event key
     *
     * @param eventKey
     *            the event key to subscribe
     * @param commandEntity
     *            the subscribed command, which its callback will be invoked upon event
     */
    public void subscribe(String eventKey, CommandEntity commandEntity) {
        coco.get().subscribe(eventKey, commandEntity);
    }

    /**
     * Get the ids and the statuses of tasks existing on the storage pool.
     * @param storagePoolId the id of the storage pool
     */
    public Map<Guid, AsyncTaskStatus> getAllTasksStatuses(Guid storagePoolId) {
        return coco.get().getAllTasksStatuses(storagePoolId);
    }

    /**
     * Get the ids of tasks existing on the storage pool that match the required status.
     * @param storagePoolId the id of the storage pool
     * @param taskStatus the required task status
     */
    public List<Guid> getTasksIdsByStatus(Guid storagePoolId, AsyncTaskStatusEnum taskStatus) {
        Map<Guid, AsyncTaskStatus> tasksStatuses = getAllTasksStatuses(storagePoolId);
        if (tasksStatuses == null) {
            return null;
        }

        return tasksStatuses.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getStatus() == taskStatus)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
