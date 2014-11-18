package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>PermissionsDAO</code> defines a type for performing CRUD operations on instances of {@link org.ovirt.engine.core.common.businessentities.Permissions}.
 *
 *
 */
public interface PermissionDAO extends GenericDao<Permissions, Guid> {
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
    Permissions getForRoleAndAdElementAndObject(Guid roleid, Guid elementid,
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
    Permissions getForRoleAndAdElementAndObjectWithGroupCheck(Guid roleid, Guid elementid,
            Guid objectid);

    /**
     * Get all permissions to consume from quota
     *
     * @param quotaId
     *            The quota Id to consume from.
     * @return All Permissions to consume from quota Id
     */
    List<Permissions> getConsumedPermissionsForQuotaId(Guid quotaId);

    /**
     * Gets all permissions for the specified AD element, including permissions of groups that it is in.
     * @param id
     *            the AD element
     * @return the list of permissions
     */
    List<Permissions> getAllForAdElement(Guid id);

    /**
     * Gets all permissions for the specified AD element, including permissions of groups that it is in,
     * with optional filtering according to the permissions of the issuing user.
     *
     * @param id
     *            the AD element
     * @param engineSessionSeqId
     *            the ID of the user session requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of permissions
     */
    List<Permissions> getAllForAdElement(Guid id, long engineSessionSeqId, boolean isFiltered);

    /**
     * Gets all permissions for the specified AD element only, excluding permissions of groups that it is in.
     * @param id
     *            the AD element
     * @return the list of permissions
     */
    List<Permissions> getAllDirectPermissionsForAdElement(Guid id);

    /**
     * Gets all permissions for the specified role.
     *
     * @param id
     *            the role
     * @return the list of permissions
     */
    List<Permissions> getAllForRole(Guid id);

    /**
     * Retrieves all permissions for the specified role and element.
     *
     * @param roleid
     *            the role
     * @param elementid
     *            the element
     * @return the list of permissions
     */
    List<Permissions> getAllForRoleAndAdElement(Guid roleid, Guid elementid);

    /**
     * Retrieves all permissions for the specified role and object.
     *
     * @param roleid
     *            the role
     * @param objectid
     *            the object
     * @return the list of permissions
     */
    List<Permissions> getAllForRoleAndObject(Guid roleid, Guid objectid);

    /**
     * Retrieves all permissions for the specified entity.
     *
     * @param id
     *            the entity
     * @return the list of permissions
     */
    List<Permissions> getAllForEntity(Guid id);

    /**
     * Retrieves all permissions for the specified entity,
     * with optional filtering according to the permissions of the issuing user.
     *
     * @param id
     *            the entity
     * @param sessionSeqId
     *            the ID of the user session requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of permissions
     */
    List<Permissions> getAllForEntity(Guid id, long sessionSeqId, boolean isFiltered);


    public List<Permissions> getAllForEntity(Guid id, long engineSessionSeqId, boolean isFiltered, boolean allUsersWithPermission);

    List<Permissions> getAllForEntity(Guid id, long engineSessionSeqId, boolean isFiltered, boolean allUsersWithPermission, int appMode);

    List<Permissions> getTreeForEntity(Guid id, VdcObjectType type);

    List<Permissions> getTreeForEntity(Guid id, VdcObjectType type, long engineSessionSeqId, boolean isFiltered);

    List<Permissions> getTreeForEntity(Guid id, VdcObjectType type, long engineSessionSeqId, boolean isFiltered, int appMode);

    Guid getEntityPermissions(Guid adElementId, ActionGroup actionGroup, Guid objectId, VdcObjectType vdcObjectType);

    Guid getEntityPermissionsForUserAndGroups(Guid userId,
                                              String groupIds,
                                              ActionGroup actionGroup,
                                              Guid objectId,
                                              VdcObjectType vdcObjectType,
                                              boolean ignoreEveryone);

    /**
     * Removes all permissions for the given entity.
     *
     * @param id
     *            the entity
     */
    void removeForEntity(Guid id);
}
