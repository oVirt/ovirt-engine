package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;

public abstract class PermissionsCommandBase<T extends PermissionsOperationsParameters> extends CommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected PermissionsCommandBase(Guid commandId) {
        super(commandId);
    }

    public PermissionsCommandBase(T parameters) {
        this(parameters, null);
    }

    public PermissionsCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected DbUser dbUser;
    protected DbGroup dbGroup;

    /**
     * Get the object translated type (e.g Host , VM), on which the MLA operation has been executed on.
     *
     * @see VdcObjectType
     * @return Translated object type.
     */
    public String getVdcObjectType() {
        return getParameters().getPermission().getObjectType().getVdcObjectTranslation();
    }

    /**
     * Get the object name, which the MLA operation occurs on. If no entity found, returns null.
     *
     * @return Object name.
     */
    public String getVdcObjectName() {
        Permissions perms = getParameters().getPermission();
        return getDbFacade().getEntityNameByIdAndType(perms.getObjectId(), perms.getObjectType());
    }

    public String getRoleName() {
        Role role = getRoleDao().get(getParameters().getPermission().getrole_id());
        return role == null ? null : role.getname();
    }

    public String getSubjectName() {
        // we may have to load user/group from db first.
        // it would be nice to handle this from command execution rather than
        // audit log messages
        initUserAndGroupData();
        return dbUser == null ? (dbGroup == null ? "" : dbGroup.getName()) : dbUser.getLoginName();
    }

    public void initUserAndGroupData() {
        if (dbUser == null) {
            dbUser = getDbUserDAO().get(getParameters().getPermission().getad_element_id());
        }
        if (dbGroup == null) {
            dbGroup = getAdGroupDAO().get(getParameters().getPermission().getad_element_id());
        }
    }

    protected boolean isSystemSuperUser() {
        Permissions superUserPermission =
                getPermissionDAO()
                        .getForRoleAndAdElementAndObjectWithGroupCheck(
                                PredefinedRoles.SUPER_USER.getId(),
                                getCurrentUser().getId(),
                                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        return superUserPermission != null;
    }

    // TODO - this code is shared with addPermissionCommand - check if
    // addPermission can extend this command
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        Permissions permission = getParameters().getPermission();
        permissionList.add(new PermissionSubject(permission.getObjectId(),
                permission.getObjectType(),
                getActionType().getActionGroup()));
        return permissionList;
    }
}
