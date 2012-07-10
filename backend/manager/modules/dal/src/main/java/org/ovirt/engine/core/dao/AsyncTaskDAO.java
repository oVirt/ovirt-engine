package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AsyncTaskDAO</code> defines a type which performs CRUD operations on instances of {@link async_tasks}.
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
    async_tasks get(Guid id);

    /**
     * Gets async task Ids by a given entity Id.
     * @param entityId the Id of the entity to return the tasks for
     * @return
     */
    List<Guid> getAsyncTaskIdsByEntity(Guid entityId);


    /**
     * Retrieves all tasks.
     *
     * @return the list of tasks
     */
    List<async_tasks> getAll();

    /**
     * Saves the specified task.
     *
     * @param task the task
     * @param enitytType type of entities to be associated with the task
     * @param entityIds IDs of entities to be associated with the task
     */
    void save(async_tasks task, VdcObjectType enitytType, Guid... entityIds);

    /**
     * Updates the specified task.
     *
     * @param task the task
     */
    void update(async_tasks task);

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
    void save(async_tasks newAsyncTask);

}
