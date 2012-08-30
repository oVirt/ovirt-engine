package org.ovirt.engine.core.dao;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Data Access Object which supports mass operations for the given entity type.
 *
 * @param T
 *            the type of entity to perform mass operations on.
 */
public interface MassOperationsDao<T extends BusinessEntity<?>> {

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
     * @param procedureName procedure name for update
     * @param entities
     */
    void updateAll(String procedureName, Collection<T> entities);
}
