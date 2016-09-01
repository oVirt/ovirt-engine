package org.ovirt.engine.core.dao;


public interface CheckedUpdate<T> {
    /**
     * Update entity but check database before update to make sure update is necessary
     * @param entity
     *            - the entity to update
     */
    void updateIfNeeded(T entity);

}
