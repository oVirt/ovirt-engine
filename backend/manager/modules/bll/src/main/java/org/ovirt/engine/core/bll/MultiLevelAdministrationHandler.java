package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class caches config values for used with many commands
 *
 */

@Singleton
public class MultiLevelAdministrationHandler {

    public static final Guid SYSTEM_OBJECT_ID = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    public static final Guid EVERYONE_OBJECT_ID = new Guid("EEE00000-0000-0000-0000-123456789EEE");
    /*
     *  bottom is an object which all the objects in the system are its parents
     *  useful to denote we want all objects when checking for permissions
     */
    public static final Guid BOTTOM_OBJECT_ID = new Guid("BBB00000-0000-0000-0000-123456789BBB");

    private static final Logger log = LoggerFactory.getLogger(MultiLevelAdministrationHandler.class);

    @Inject
    private PermissionDao permissionDao;

    @Inject
    private RoleDao roleDao;

    @Inject
    private DbUserDao dbUserDao;

    /**
     * Admin user is a user with at least one permission that contains admin
     * role
     *
     * @return True if user is admin
     */
    public boolean isAdminUser(DbUser user) {
        List<Role> userRoles =
                roleDao.getAnyAdminRoleForUserAndGroups(user.getId(), StringUtils.join(user.getGroupIds(), ","));
        if (!userRoles.isEmpty()) {
            log.debug("LoginAdminUser: User logged to admin using role '{}'", userRoles.get(0).getName());
            return true;
        }
        return false;
    }

    public void addPermission(Permission... permissions) {
        for (Permission perms : permissions) {
            permissionDao.save(perms);
        }
    }

    public void setIsAdminGUIFlag(Guid userId, boolean hasPermissions) {
        DbUser user = dbUserDao.get(userId);
        if (user.isAdmin() != hasPermissions) {
            user.setAdmin(hasPermissions);
            dbUserDao.update(user);
        }
    }

    /**
     * Checks if supplied role is the last (or maybe only) role with super user privileges.
     *
     * @param roleId
     *               the role id.
     * @return true if role is the last with Super User privileges, otherwise, false
     */
    public boolean isLastSuperUserPermission(Guid roleId) {
        boolean retValue=false;
        if (PredefinedRoles.SUPER_USER.getId().equals(roleId)) {
            // check that there is at least one super-user left in the system
            List<Permission> permissions = permissionDao.getAllForRole(
                    PredefinedRoles.SUPER_USER.getId());
            if (permissions.size() <= 1) {
                retValue = true;
            }
        }
        return retValue;
    }

    /**
     * Checks if supplied group is the last (or maybe only)  with super user privileges.
     *
     * @param groupId
     *                the group is
     * @return true if group is the last with Super User privileges, otherwise, false
     */
    public boolean isLastSuperUserGroup(Guid groupId) {
        boolean retValue=false;
        // check that there is at least one super-user left in the system
        List<Permission> permissions = permissionDao.getAllForRole(
                PredefinedRoles.SUPER_USER.getId());
        if (permissions.size() <= 1) {
            // get group role
            permissions = permissionDao.getAllForAdElement(groupId);
            for (Permission permission : permissions){
                if (permission.getRoleId().equals(PredefinedRoles.SUPER_USER.getId())){
                    retValue = true;
                    break;
                }
            }
        }
        return retValue;
    }

    public static boolean isMultilevelAdministrationOn() {
        return Config.<Boolean> getValue(ConfigValues.IsMultilevelAdministrationOn);
    }

    public void addPermission(Guid userId, Guid entityId, PredefinedRoles role, VdcObjectType objectType) {
        Permission perms = new Permission();
        perms.setAdElementId(userId);
        perms.setObjectType(objectType);
        perms.setObjectId(entityId);
        perms.setRoleId(role.getId());
        addPermission(perms);
    }
}
