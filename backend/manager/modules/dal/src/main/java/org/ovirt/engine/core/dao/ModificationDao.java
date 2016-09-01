package org.ovirt.engine.core.dao;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Data Access Object interface used for modifying entities persisted to a store of data. Extending types could provide
 * more entity-specific methods for modifying the entity in more complex situations (i.e. batch operations).
 *
 * @param <T>
 *            The type of the entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public interface ModificationDao<T extends BusinessEntity<ID>, ID extends Serializable> extends Dao {

    /**
     * Persist a new instance of the entity.
     *
     * @param entity
     *            The entity to persist (can't be {@code null}).
     */
    public void save(T entity);

    /**
     * Update an existing entity with data from the given instance.
     *
     * @param entity
     *            The entity instance, containing data to update (can't be {@code null}).
     */
    public void update(T entity);

    /**
     * Removes the entity with the given id from the underlying store of data.
     *
     * @param id
     *            The id of the entity to remove (can't be {@code null}).
     */
    public void remove(ID id);
}
