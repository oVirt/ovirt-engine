package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * Defines a type that performs CRUD operations on instances of {@link DbGroup}.
 */
public interface DbGroupDao extends Dao, SearchDao<DbGroup> {
    /**
     * Retrieves the instance with the specified id.
     *
     * @param id
     *            the group id
     * @return the group
     */
    DbGroup get(Guid id);

    /**
     * Retrieves a group by domain name and external identifier.
     *
     * @param domain the name of the domain
     * @param externalId the external identifier
     * @return a reference to the group or {@code null} if no such group
     *   can be found in the database
     */
    DbGroup getByExternalId(String domain, String externalId);

    /**
     * Retrieves a group that matches either the internal or external identifiers given.
     *
     * @param id the internal identifier
     * @param domain the name of the domain
     * @param externalId the external identifier
     * @return a reference to the group or {@code null} if no such group
     *   can be found in the database
     */
    DbGroup getByIdOrExternalId(Guid id, String domain, String externalId);

    /**
     * Retrieves the group with the specified name.
     *
     * @param name
     *            the group name
     * @return the group
     */
    DbGroup getByName(String name);

    /**
     * Retrieves the group with the specified name and domain.
     *
     * @param name
     *            the group name
     * @param domain
     *            the domain name
     * @return the group
     */
    DbGroup getByNameAndDomain(String name, String domain);

    /**
     * Retrieves all groups.
     *
     * @return the list of all groups
     */
    List<DbGroup> getAll();

    /**
     * Saves the supplied group.
     *
     * @param group
     *            the group
     */
    void save(DbGroup group);

    /**
     * Updates the supplied group.
     *
     * @param group
     *            the group
     */
    void update(DbGroup group);

    /**
     * Removes the group with the specified id.
     *
     * @param id
     *            the group id
     */
    void remove(Guid id);

}
