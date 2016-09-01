package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Data Access Object interface used for reading entities from a source of data. Extending types should provide more
 * entity-specific methods for retrieving the entity in more complex situations (i.e. by a field of the entity).
 *
 * @param <T>
 *            The type of the entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public interface ReadDao<T extends BusinessEntity<ID>, ID extends Serializable> extends Dao {

    /**
     * Retrieves the entity with the given id.
     *
     * @param id
     *            The id to look by (can't be {@code null}).
     * @return The entity instance, or {@code null} if not found.
     */
    public T get(ID id);

    /**
     * Retrieves all the entities of type {@link T}.
     *
     * @return A list of all the entities, or an empty list if none is found.
     */
    public List<T> getAll();
}
