package org.ovirt.engine.core.dao;

import java.util.List;

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
     * @param id
     *            the task id
     * @return the task
     */
    async_tasks get(Guid id);

    /**
     * Retrieves all tasks.
     *
     * @return the list of tasks
     */
    List<async_tasks> getAll();

    /**
     * Saves the specified task.
     *
     * @param task
     *            the task
     */
    void save(async_tasks task);

    /**
     * Updates the specified task.
     *
     * @param task
     *            the task
     */
    void update(async_tasks task);

    /**
     * Removes the task with the specified id.
     *
     * @param id
     */
    int remove(Guid id);
}
