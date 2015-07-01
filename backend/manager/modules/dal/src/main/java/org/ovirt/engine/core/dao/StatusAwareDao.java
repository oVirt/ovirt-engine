package org.ovirt.engine.core.dao;

import java.io.Serializable;

/**
 * This is a specific type of Dao that is aware of entities having a "status" field, and can update this field.
 *
 * @param <ID>
 *            The type of the entity's id.
 * @param <S>
 *            The type of status.
 * @see GenericDao
 */
public interface StatusAwareDao<ID extends Serializable, S extends Enum<?>> extends Dao {
    /**
     * Update the entity's status field only.
     *
     * @param id
     *            The id of the entity for which to update the status field.
     * @param status
     *            The status to update to.
     */
    public void updateStatus(ID id, S status);
}
