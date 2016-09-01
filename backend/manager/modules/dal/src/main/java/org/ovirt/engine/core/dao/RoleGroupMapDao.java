package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code RoleGroupMapDao} defines a type for performing CRUD operations on instances of {@link RoleGroupMap}.
 */
public interface RoleGroupMapDao extends Dao {
    /**
     * Retrieves the mapping for the given group and role id.
     *
     * @param group
     *            the action group
     * @param id
     *            the role id
     * @return the mapping
     */
    RoleGroupMap getByActionGroupAndRole(ActionGroup group, Guid id);

    /**
     * Retrieves the list of mappings for the given role id.
     *
     * @param id
     *            the role id
     * @return the list of mappings
     */
    List<RoleGroupMap> getAllForRole(Guid id);

    /**
     * Saves the specified map.
     *
     * @param map
     *            the map
     */
    void save(RoleGroupMap map);

    /**
     * Removes the specified map.
     *
     * @param group
     *            the action group
     * @param id
     *            the role id
     */
    void remove(ActionGroup group, Guid id);
}
