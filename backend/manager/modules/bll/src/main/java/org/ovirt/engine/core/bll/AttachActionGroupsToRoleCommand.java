package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AttachActionGroupsToRoleCommand<T extends ActionGroupsToRoleParameter> extends RolesCommandBase<T> {
    private static final long serialVersionUID = -8023416661994574621L;

    public AttachActionGroupsToRoleCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        List<ActionGroup> attachGroups = getParameters().getActionGroups();
        Guid roleId = getParameters().getRoleId();
        roles role = getRoleDao().get(roleId);

        List<String> canDoMessages = getReturnValue().getCanDoActionMessages();
        if (checkIfRoleIsReadOnly(canDoMessages)) {
            canDoMessages.add(VdcBllMessages.VAR__TYPE__ROLE.toString());
            canDoMessages.add(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO.toString());
            return false;
        }

        // Get all groups by ID and check if they already exist
        List<ActionGroup> allGroups = getActionGroupsByRoleId(roleId);
        for (ActionGroup group : attachGroups) {
            if (allGroups.contains(group)) {
                // group already exist
                canDoMessages.add(
                        VdcBllMessages.ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED.toString());
                return false;
            } else if (role.getType() != RoleType.ADMIN && group.getRoleType() == RoleType.ADMIN) {
                canDoMessages.add(
                        VdcBllMessages.CANNOT_ADD_ACTION_GROUPS_TO_ROLE_TYPE.toString());
                return false;
            }

        }

        return true;
    }

    @Override
    protected void executeCommand() {
        List<ActionGroup> groups = getParameters().getActionGroups();
        for (ActionGroup group : groups) {
            getRoleGroupMapDAO().save(new RoleGroupMap(group, getParameters().getRoleId()));
            AppendCustomValue("ActionGroup", group.toString(), ", ");
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACHED_ACTION_GROUP_TO_ROLE
                : AuditLogType.USER_ATTACHED_ACTION_GROUP_TO_ROLE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
