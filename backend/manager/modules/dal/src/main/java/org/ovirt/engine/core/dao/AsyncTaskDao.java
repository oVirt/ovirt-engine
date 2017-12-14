package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code AsyncTaskDao} defines a type which performs CRUD operations on instances of {@link AsyncTask}.
 */
public interface AsyncTaskDao extends Dao {
    /**
     * Retrieves the task with the specified id.
     *
     * @param id the task id
     * @return the task
     */
    AsyncTask get(Guid id);

    /**
     * Retrieves the task with the specified VDSM taskId
     * @param vdsmTaskId id of the task as reported by VDSM
     * @return the task
     */
    AsyncTask getByVdsmTaskId(Guid vdsmTaskId);
    /**
     * Gets async task Ids by a given entity Id.
     * @param entityId the Id of the entity to return the tasks for
     */
    List<Guid> getAsyncTaskIdsByEntity(Guid entityId);

    /**
     * Gets async task Ids of tasks that are running
     * on the given storage pool
     * @param storagePoolId ID of storage pool to return running tasks for
     */
    List<AsyncTask> getAsyncTaskIdsByStoragePoolId(Guid storagePoolId);


    /**
     * Retrieves all tasks.
     *
     * @return the list of tasks
     */
    List<AsyncTask> getAll();

    /**
     * Saves or updates the specified task
     *
     * @param task the task
     */
    void saveOrUpdate(AsyncTask task);

    /**
     * Updates the specified task.
     *
     * @param task the task
     */
    void update(AsyncTask task);

    /**
     * Removes the task with the specified id.
     */
    int remove(Guid id);

    /**
     * Saves the specified task.
     *
     * @param newAsyncTask the task to save
     */
    void save(AsyncTask newAsyncTask);

    /**
     * Removes the specified task according to its VDSM task id.
     * @param vdsmTaskId the VDSM task id.
     * @return the number of tasks deleted
     */
    int removeByVdsmTaskId(Guid vdsmTaskId);

    /**
     * Gets all the tasks that are associated with an entity id
     */
    List<AsyncTask> getTasksByEntity(Guid entityId);

    void insertAsyncTaskEntities(Collection<AsyncTaskEntity> asyncTaskEntities);

    List<AsyncTaskEntity> getAllAsyncTaskEntitiesByTaskId(Guid taskId);
}
