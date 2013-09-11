package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;

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
     * @param entities
     */
    void updateAll(String procedureName, Collection<T> entities);

    /**
     * Removes the entities with given ids
     *
     * @param ids
     */
    void removeAll(Collection<ID> ids);

    /**
     * Calls an update stored procedure multiple timse in a batch
     *
     * @param procedureName
     * @param entities
     */
    void updateAllInBatch(String procedureName, Collection<T> paramValues, MapSqlParameterMapper<T> mapper);

    /**
     * Saves the given entities using a more efficient method to save all of them at once, rather than each at a time.
     * The procedure name to be used is "InsertEntityName" where EntityName stands for the name of the entity
     *
     * @param entities
     *            The entities to insert
     */
    void saveAll(Collection<T> entities);
}
