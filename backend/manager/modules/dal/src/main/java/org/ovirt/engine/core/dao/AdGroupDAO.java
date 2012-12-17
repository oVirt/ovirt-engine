package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AdGroupDAO</code> defines a type that performs CRUD operations on instances of {@link LdapGroup}.
 *
 *
 */
public interface AdGroupDAO extends DAO {
    /**
     * Retrieves the instance with the specified id.
     *
     * @param id
     *            the group id
     * @return the group
     */
    LdapGroup get(Guid id);

    /**
     * Retrieves the group with the specified name.
     *
     * @param name
     *            the group name
     * @return the group
     */
    LdapGroup getByName(String name);

    /**
     * Retrieves all groups.
     *
     * @return the list of all groups
     */
    List<LdapGroup> getAll();

    /**
     * Retrieves all time leased groups for the specified pool.
     *
     * @param id
     *            the pool
     * @return the list of groups
     */
    List<LdapGroup> getAllTimeLeasedForPool(int id);

    /**
     * Saves the supplied group.
     *
     * @param group
     *            the group
     */
    void save(LdapGroup group);

    /**
     * Updates the supplied group.
     *
     * @param group
     *            the group
     */
    void update(LdapGroup group);

    /**
     * Removes the group with the specified id.
     *
     * @param id
     *            the group id
     */
    void remove(Guid id);
}
