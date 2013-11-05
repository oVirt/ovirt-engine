package org.ovirt.engine.core.dao;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

public interface CachedDao<T extends BusinessEntity<ID>, ID extends Serializable> {
    /**
     * Retrieves the entity with the given id from the cache.
     * @param id
     *            The id to look by (can't be <code>null</code>).
     * @return The entity instance, or <code>null</code> if not found.
     */
    public T getFromCache(ID id);

}
