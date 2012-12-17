package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("RoleName"), @CustomLogField("VdcObjectType"), @CustomLogField("VdcObjectName"),
        @CustomLogField("SubjectName") })
public abstract class PermissionsCommandBase<T extends PermissionsOperationsParametes> extends CommandBase<T> {
    private static final long serialVersionUID = -2515073672998232982L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected PermissionsCommandBase(Guid commandId) {
        super(commandId);
    }

    public PermissionsCommandBase(T parameters) {
        super(parameters);
    }

    protected DbUser _dbUser;
    protected LdapGroup _adGroup;

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
        permissions perms = getParameters().getPermission();
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
        return _dbUser == null ? (_adGroup == null ? "" : _adGroup.getname()) : _dbUser.getusername();
    }

    public void initUserAndGroupData() {
        if (_dbUser == null) {
            _dbUser = getDbUserDAO().get(getParameters().getPermission().getad_element_id());
        }
        if (_adGroup == null) {
            _adGroup = getAdGroupDAO().get(getParameters().getPermission().getad_element_id());
        }
    }

    protected boolean isSystemSuperUser() {
        permissions superUserPermission =
                getPermissionDAO()
                        .getForRoleAndAdElementAndObjectWithGroupCheck(
                                PredefinedRoles.SUPER_USER.getId(),
                                getCurrentUser().getUserId(),
                                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        return superUserPermission != null;
    }

    // TODO - this code is shared with addPermissionCommand - check if
    // addPermission can extend this command
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissions permission = getParameters().getPermission();
        permissionList.add(new PermissionSubject(permission.getObjectId(),
                permission.getObjectType(),
                getActionType().getActionGroup()));
        return permissionList;
    }
}
