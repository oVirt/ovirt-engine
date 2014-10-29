package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AsyncTaskDAO</code> defines a type which performs CRUD operations on instances of {@link org.ovirt.engine.core.common.businessentities.AsyncTask}.
 *
 *
 */
public interface AsyncTaskDAO extends DAO {
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
     * @return
     */
    List<Guid> getAsyncTaskIdsByEntity(Guid entityId);

    /**
     * Gets async task Ids of tasks that are running
     * on the given storage pool
     * @param storagePoolId ID of storage pool to return running tasks for
     * @return
     */
    List<Guid> getAsyncTaskIdsByStoragePoolId(Guid storagePoolId);


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
     *
     * @param id
     */
    int remove(Guid id);

    /**
     * Saves the specified task.
     *
     * @param task the task to save
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
     *
     * @param entityId
     * @return
     */
    List<AsyncTask> getTasksByEntity(Guid entityId);

    /**
     * Gets all the async task ids that are associated with a user id
     *
     * @param userId
     * @return
     */
    public List<Guid> getAsyncTaskIdsByUser(Guid userId);

    /**
     * Gets all the vdsm task ids that are associated with a user id
     *
     * @param userId
     * @return
     */
    public List<Guid> getVdsmTaskIdsByUser(Guid userId);

    void insertAsyncTaskEntities(Collection<AsyncTaskEntity> asyncTaskEntities);

    List<AsyncTaskEntity> getAllAsyncTaskEntitiesByTaskId(Guid taskId);
}
