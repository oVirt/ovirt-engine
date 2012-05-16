package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class DetachActionGroupsFromRoleCommand<T extends ActionGroupsToRoleParameter> extends RolesCommandBase<T> {
    private static final long serialVersionUID = -6769273368646220254L;

    public DetachActionGroupsFromRoleCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        Guid roleId = getParameters().getRoleId();
        roles role = getRoleDao().get(roleId);
        if (role == null) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED);
            return false;
        }

        List<String> canDoMessages = getReturnValue().getCanDoActionMessages();
        if (checkIfRoleIsReadOnly(canDoMessages)) {
            canDoMessages.add(VdcBllMessages.VAR__TYPE__ROLE.toString());
            canDoMessages.add(VdcBllMessages.VAR__ACTION__DETACH_ACTION_TO.toString());
            return false;
        }

        List<ActionGroup> groupsToDetach = getParameters().getActionGroups();
        List<ActionGroup> allGroups = getActionGroupsByRoleId(roleId);

        // Check that target action group exists for this role
        for (ActionGroup group : groupsToDetach) {
            if (!allGroups.contains(group)) {
                canDoMessages.add(
                        VdcBllMessages.ERROR_CANNOT_DETACH_ACTION_GROUP_TO_ROLE_NOT_ATTACHED.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        List<ActionGroup> groupsToDetach = getParameters().getActionGroups();
        for (ActionGroup group : groupsToDetach) {
            getRoleGroupMapDAO().remove(group, getParameters().getRoleId());
            AppendCustomValue("ActionGroup", group.toString(), ", ");
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE
                : AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE_FAILED;
    }
}
