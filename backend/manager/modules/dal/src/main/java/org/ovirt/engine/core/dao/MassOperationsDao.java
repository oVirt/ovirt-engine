package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Data Access Object which supports mass operations for the given entity type.
 *
 * @param T
 *            the type of entity to perform mass operations on.
 *
 * @param <ID>
 *            The type of the entity's id.
 */
public interface MassOperationsDao<T extends BusinessEntity<?>, ID extends Serializable> {

    /**
     * Updates the given entities using a more efficient method to update all of them at once, rather than each at a
     * time. The procedure name to be used is "UpdateEntityName> where EntityName stands for the name of the entity
     *
     * @param entities
     *            The entities to update.
     */
    void updateAll(Collection<T> entities);

    /**
     * Updates the given entities using a more efficient method to update all of them at once, rather than each at a
     * time.
     *
     * @param procedureName
     *            procedure name for update
     */
    void updateAll(String procedureName, Collection<T> entities);

    /**
     * Removes the entities with given ids
     */
    void removeAll(Collection<ID> ids);

    /**
     * Calls a remove stored procedure multiple times in a batch
     */
    void removeAllInBatch(Collection<T> entities);

    /**
     * Calls an update stored procedure multiple times in a batch
     */
    void updateAllInBatch(Collection<T> entities);

    /**
     * Calls an insert stored procedure multiple times
     */
    void saveAll(Collection<T> entities);

    /**
     * Saves the given entities using a more efficient method to save all of them at once, rather than each at a time.
     * The procedure name to be used is "InsertEntityName" where EntityName stands for the name of the entity
     *
     * @param entities
     *            The entities to insert
     */
    void saveAllInBatch(Collection<T> entities);
}
