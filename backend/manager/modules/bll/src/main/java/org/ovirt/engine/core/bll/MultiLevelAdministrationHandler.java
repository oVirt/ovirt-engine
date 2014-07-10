package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.RoleDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class caches config values for used with many commands
 *
 */
public class MultiLevelAdministrationHandler {

    public static final Guid SYSTEM_OBJECT_ID = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    public static final Guid EVERYONE_OBJECT_ID = new Guid("EEE00000-0000-0000-0000-123456789EEE");
    /*
     *  bottom is an object which all the objects in the system are its parents
     *  useful to denote we want all objects when checking for permissions
     */
    public static final Guid BOTTOM_OBJECT_ID = new Guid("BBB00000-0000-0000-0000-123456789BBB");

    private static final Log log = LogFactory.getLog(MultiLevelAdministrationHandler.class);

    public static PermissionDAO getPermissionDAO() {
        return DbFacade.getInstance().getPermissionDao();
    }

    public static RoleDAO getRoleDAO() {
        return DbFacade.getInstance().getRoleDao();
    }

    public static DbUserDAO getDbUserDAO() {
        return DbFacade.getInstance().getDbUserDao();
    }

    /**
     * Admin user is a user with at least one permission that contains admin
     * role
     *
     * @param userId
     * @return True if user is admin
     */
    public static boolean isAdminUser(DbUser user) {
        List<Role> userRoles =
                getRoleDAO().getAllForUserAndGroups(user.getId(), StringUtils.join(user.getGroupIds(), ","));

        for (Role r : userRoles) {
            if (r.getType() == RoleType.ADMIN) {
                if (log.isDebugEnabled()) {
                    log.debugFormat("LoginAdminUser: User logged to admin using role {0}", r.getname());
                }
                return true;
            }
        }
        return false;
    }

    public static void addPermission(Permissions... permissions) {
        for (Permissions perms : permissions) {
            getPermissionDAO().save(perms);
        }
    }

    /**
     * Set the user lastAdminStatusCheck flag to the value specified
     *
     * @param userId
     * @param hasPermissions
     *            will saved as {@link DbUser.lastAdminStatusCheck} value
     * @see {@link DbUser}
     */
    public static void setIsAdminGUIFlag(Guid userId, boolean hasPermissions) {
        DbUser user = getDbUserDAO().get(userId);
        if (user.isAdmin() != hasPermissions) {
            user.setAdmin(hasPermissions);
            getDbUserDAO().update(user);
        }
    }

    /**
     * Checks if supplied role is the last (or maybe only) role with super user privileges.
     *
     * @param roleId
     *               the role id.
     * @return true if role is the last with Super User privileges, otherwise, false
     */
    public static boolean isLastSuperUserPermission(Guid roleId) {
        boolean retValue=false;
        if (PredefinedRoles.SUPER_USER.getId().equals(roleId)) {
            // check that there is at least one super-user left in the system
            List<Permissions> permissions = getPermissionDAO().getAllForRole(
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
     * @param group_id
     *                the group is
     * @return true if group is the last with Super User privileges, otherwise, false
     */
    public static boolean isLastSuperUserGroup(Guid groupId) {
        boolean retValue=false;
        // check that there is at least one super-user left in the system
        List<Permissions> permissions = getPermissionDAO().getAllForRole(
                PredefinedRoles.SUPER_USER.getId());
        if (permissions.size() <= 1) {
            // get group role
            permissions = getPermissionDAO().getAllForAdElement(groupId);
            for (Permissions permission : permissions){
                if (permission.getrole_id().equals(PredefinedRoles.SUPER_USER.getId())){
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

    public static void addPermission(Guid userId, Guid entityId, PredefinedRoles role, VdcObjectType objectType) {
        Permissions perms = new Permissions();
        perms.setad_element_id(userId);
        perms.setObjectType(objectType);
        perms.setObjectId(entityId);
        perms.setrole_id(role.getId());
        addPermission(perms);
    }
}
