package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("RoleName") })
public abstract class RolesCommandBase<T extends RolesParameterBase> extends CommandBase<T> {
    private roles _role;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public RolesCommandBase(Guid commandId) {
        super(commandId);
    }

    public RolesCommandBase(T parameters) {
        super(parameters);
    }

    protected roles getRole() {
        if (_role == null) {
            _role = DbFacade.getInstance().getRoleDAO().get(getParameters().getRoleId());
        }
        return _role;
    }

    public String getRoleName() {
        return getRole().getname();
    }

    protected boolean CheckIfRoleIsReadOnly(java.util.ArrayList<String> CanDoActionMessages) {
        boolean result = false;
        if (DbFacade.getInstance().getRoleDAO().get(getParameters().getRoleId()).getis_readonly()) {
            result = true;
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_ROLE_IS_READ_ONLY.toString());
        }
        return result;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getParameters().getRoleId(), VdcObjectType.Role);
    }

    protected ArrayList<ActionGroup> getActionGroupsByRoleId(Guid roleId) {
        ArrayList<ActionGroup> allGroups = new ArrayList<ActionGroup>();
        List<RoleGroupMap> allGroupsMaps = DbFacade.getInstance().getRoleGroupMapDAO().getAllForRole(roleId);
        for (RoleGroupMap map : allGroupsMaps) {
            allGroups.add(map.getActionGroup());
        }
        return allGroups;
    }
}
