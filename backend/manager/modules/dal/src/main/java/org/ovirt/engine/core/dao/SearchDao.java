package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Queryable;

/**
 * Generic interface for entities that needs to be searched with an SQL query.
 */
public interface SearchDao<T extends Queryable> {
    /**
     * Finds all entities using a supplied SQL query.
     *
     * @param query
     *            the query
     * @return the list of entries
     */
    List<T> getAllWithQuery(String query);
}
