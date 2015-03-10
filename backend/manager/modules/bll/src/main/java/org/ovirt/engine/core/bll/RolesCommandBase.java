package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class RolesCommandBase<T extends RolesParameterBase> extends CommandBase<T> {
    private Role _role;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public RolesCommandBase(Guid commandId) {
        super(commandId);
    }

    public RolesCommandBase(T parameters) {
        this(parameters, null);
    }

    public RolesCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected Role getRole() {
        if (_role == null) {
            _role = getRoleDao().get(getParameters().getRoleId());
        }
        return _role;
    }

    public String getRoleName() {
        return getRole().getName();
    }

    protected boolean checkIfRoleIsReadOnly(List<String> CanDoActionMessages) {
        boolean result = false;
        if (getRole().isReadonly()) {
            result = true;
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_ROLE_IS_READ_ONLY.toString());
        }
        return result;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getRoleId(),
                VdcObjectType.Role,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected List<ActionGroup> getActionGroupsByRoleId(Guid roleId) {
        List<ActionGroup> allGroups = new ArrayList<ActionGroup>();
        List<RoleGroupMap> allGroupsMaps = getRoleGroupMapDAO().getAllForRole(roleId);
        for (RoleGroupMap map : allGroupsMaps) {
            allGroups.add(map.getActionGroup());
        }
        return allGroups;
    }
}
