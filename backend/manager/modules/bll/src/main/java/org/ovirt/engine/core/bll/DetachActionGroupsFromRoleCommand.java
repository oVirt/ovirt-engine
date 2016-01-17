package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class DetachActionGroupsFromRoleCommand<T extends ActionGroupsToRoleParameter> extends RolesCommandBase<T> {

    public DetachActionGroupsFromRoleCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (getRole() == null) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED);
            return false;
        }

        List<String> validationMessages = getReturnValue().getValidationMessages();
        if (checkIfRoleIsReadOnly(validationMessages)) {
            validationMessages.add(EngineMessage.VAR__TYPE__ROLE.toString());
            validationMessages.add(EngineMessage.VAR__ACTION__DETACH_ACTION_TO.toString());
            return false;
        }

        Guid roleId = getParameters().getRoleId();
        List<ActionGroup> allGroups = getActionGroupsByRoleId(roleId);
        List<ActionGroup> groupsToDetach = getParameters().getActionGroups();

        // Check that target action group exists for this role
        for (ActionGroup group : groupsToDetach) {
            if (!allGroups.contains(group)) {
                validationMessages.add(
                        EngineMessage.ERROR_CANNOT_DETACH_ACTION_GROUP_TO_ROLE_NOT_ATTACHED.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        List<ActionGroup> groupsToDetach = getParameters().getActionGroups();
        for (ActionGroup group : groupsToDetach) {
            getRoleGroupMapDao().remove(group, getParameters().getRoleId());
            appendCustomValue("ActionGroup", group.toString(), ", ");
        }

        // If the role didn't allow viewing children in the first place, removing action groups won't change that
        Role role = getRole();
        if (role.allowsViewingChildren()) {
            boolean shouldAllowViewingChildren = false;

            // Go over all the REMAINING action groups
            List<ActionGroup> groups = getActionGroupsByRoleId(role.getId());
            for (ActionGroup group : groups) {
                if (group.allowsViewingChildren()) {
                    shouldAllowViewingChildren = true;
                    break;
                }
            }

            if (!shouldAllowViewingChildren) {
                role.setAllowsViewingChildren(false);
                getRoleDao().update(role);
            }
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE
                : AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE_FAILED;
    }
}
