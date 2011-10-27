package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AdGroupDAO</code> defines a type that performs CRUD operations on instances of {@link ad_groups}.
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
    ad_groups get(Guid id);

    /**
     * Retrieves the group with the specified name.
     *
     * @param name
     *            the group name
     * @return the group
     */
    ad_groups getByName(String name);

    /**
     * Retrieves all groups.
     *
     * @return the list of all groups
     */
    List<ad_groups> getAll();

    /**
     * Retrieves all time leased groups for the specified pool.
     *
     * @param id
     *            the pool
     * @return the list of groups
     */
    List<ad_groups> getAllTimeLeasedForPool(int id);

    /**
     * Saves the supplied group.
     *
     * @param group
     *            the group
     */
    void save(ad_groups group);

    /**
     * Updates the supplied group.
     *
     * @param group
     *            the group
     */
    void update(ad_groups group);

    /**
     * Removes the group with the specified id.
     *
     * @param id
     *            the group id
     */
    void remove(Guid id);
}
