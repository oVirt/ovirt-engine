package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>PermissionsDAO</code> defines a type for performing CRUD operations on instances of {@link permissions}.
 *
 *
 */
public interface PermissionDAO extends GenericDao<permissions, Guid> {
    /**
     * Retrieves the permission for the specified role, element and object.
     *
     * @param roleid
     *            the role
     * @param elementid
     *            the element
     * @param objectid
     *            the object
     * @return the permission
     */
    permissions getForRoleAndAdElementAndObject(Guid roleid, Guid elementid,
                                                Guid objectid);

    /**
     * Retrieves the permission for the specified role, element and object,
     * considering the user's group's permissions as well.
     *
     * @param roleid
     *            the role
     * @param elementid
     *            the element
     * @param objectid
     *            the object
     * @return the permission
     */
    permissions getForRoleAndAdElementAndObjectWithGroupCheck(Guid roleid, Guid elementid,
                                                              Guid objectid);

    /**
     * Gets all permissions for the specified AD element, including permissions of groups that it is in.
     * @param id
     *            the AD element
     * @return the list of permissions
     */
    List<permissions> getAllForAdElement(Guid id);

    /**
     * Gets all permissions for the specified AD element only, excluding permissions of groups that it is in.
     * @param id
     *            the AD element
     * @return the list of permissions
     */
    List<permissions> getAllDirectPermissionsForAdElement(Guid id);

    /**
     * Gets all permissions for the specified role.
     *
     * @param id
     *            the role
     * @return the list of permissions
     */
    List<permissions> getAllForRole(Guid id);

    /**
     * Retrieves all permissions for the specified role and element.
     *
     * @param roleid
     *            the role
     * @param elementid
     *            the element
     * @return the list of permissions
     */
    List<permissions> getAllForRoleAndAdElement(Guid roleid, Guid elementid);

    /**
     * Retrieves all permissions for the specified role and object.
     *
     * @param roleid
     *            the role
     * @param objectid
     *            the object
     * @return the list of permissions
     */
    List<permissions> getAllForRoleAndObject(Guid roleid, Guid objectid);

    /**
     * Retrieves all permissions for the specified entity.
     *
     * @param id
     *            the entity
     * @return the list of permissions
     */
    List<permissions> getAllForEntity(Guid id);

    List<permissions> getTreeForEntity(Guid id, VdcObjectType type);

    /**
     * Removes all permissions for the given entity.
     *
     * @param id
     *            the entity
     */
    void removeForEntity(Guid id);
}
