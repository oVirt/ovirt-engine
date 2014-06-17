package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AttachActionGroupsToRoleCommand<T extends ActionGroupsToRoleParameter> extends RolesCommandBase<T> {

    public AttachActionGroupsToRoleCommand(T params) {
        this(params, null);
    }

    public AttachActionGroupsToRoleCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean canDoAction() {
        List<String> canDoMessages = getReturnValue().getCanDoActionMessages();
        if (checkIfRoleIsReadOnly(canDoMessages)) {
            canDoMessages.add(VdcBllMessages.VAR__TYPE__ROLE.toString());
            canDoMessages.add(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO.toString());
            return false;
        }

        if (checkIfGroupsCanBeAttached(canDoMessages)) {
            return false;
        }

        return true;
    }

    protected boolean checkIfGroupsCanBeAttached(List<String> canDoMessages) {
        List<ActionGroup> attachGroups = getParameters().getActionGroups();
        Guid roleId = getParameters().getRoleId();
        Role role = getRole();

        // Get all groups by ID and check if they already exist
        List<ActionGroup> allGroups = getActionGroupsByRoleId(roleId);
        for (ActionGroup group : attachGroups) {
            if (allGroups.contains(group)) {
                // group already exist
                canDoMessages.add(
                        VdcBllMessages.ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED.toString());
                return true;
            } else if (role.getType() != RoleType.ADMIN && group.getRoleType() == RoleType.ADMIN) {
                canDoMessages.add(
                        VdcBllMessages.CANNOT_ADD_ACTION_GROUPS_TO_ROLE_TYPE.toString());
                return true;
            }
        }

        return false;
    }

    @Override
    protected void executeCommand() {
        boolean addedGroupThatAllowsViewingChildren = false;

        List<ActionGroup> groups = getParameters().getActionGroups();
        for (ActionGroup group : groups) {
            addedGroupThatAllowsViewingChildren |= group.allowsViewingChildren();
            getRoleGroupMapDAO().save(new RoleGroupMap(group, getParameters().getRoleId()));
            appendCustomValue("ActionGroup", group.toString(), ", ");
        }

        // Only adding groups that allow viewing children could make a role allow viewing its children
        if (addedGroupThatAllowsViewingChildren) {
            Role role = getRole();
            // The role should be updated only if it didn't allow viewing children in the first place
            if (!role.allowsViewingChildren()) {
                role.setAllowsViewingChildren(true);
                getRoleDao().update(role);
            }
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
