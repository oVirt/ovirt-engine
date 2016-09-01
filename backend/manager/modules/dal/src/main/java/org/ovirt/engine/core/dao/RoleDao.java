package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code RoleDao} defines a type for performing CRUD operations on instances of {@link Role}.
 */
public interface RoleDao extends GenericDao<Role, Guid> {

    /**
     * Retrieves the role with the specified name.
     *
     * @param name
     *            the role name
     * @return the role
     */
    Role getByName(String name);

    /**
     * This method gets the admin Roles for the given user and its groups. The purpose of this method is to be able to get
     * roles even if the user is not already added to DB, by getting the roles for his groups
     *
     * @param userId
     *            ID of the user to obtain roles for
     * @param groupIds
     *            comma delimited list of group IDs of the user
     * @return the list of the roles
     */
    List<Role> getAnyAdminRoleForUserAndGroups(Guid userId, String groupIds);

    /**
     * This method gets the admin Roles for the given user and its groups. The purpose of this method is to be able to get
     * roles even if the user is not already added to DB, by getting the roles for his groups
     *
     * @param userId
     *            ID of the user to obtain roles for
     * @param groupIds
     *            comma delimited list of group IDs of the user
     * @param appMode
     *            application mode to obtain roles for
     * @return the list of the roles
     */
    List<Role> getAnyAdminRoleForUserAndGroups(Guid userId, String groupIds, int appMode);

    /**
     * This method retrieves all roles except admin-ones.
     *
     * @return the list of the roles
     */
    List<Role> getAllNonAdminRoles();
}
