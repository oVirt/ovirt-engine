package org.ovirt.engine.core.dao;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Generic Data Access Object which contains both methods to read entities (from {@link ReadDao}) and to modify entities
 * (from {@link ModificationDao}).
 *
 * @param <T>
 *            The type of the entity.
 * @param <ID>
 *            The type of the entity's id.
 */
public interface GenericDao<T extends BusinessEntity<ID>, ID extends Serializable>
        extends ReadDao<T, ID>, ModificationDao<T, ID> {
}
