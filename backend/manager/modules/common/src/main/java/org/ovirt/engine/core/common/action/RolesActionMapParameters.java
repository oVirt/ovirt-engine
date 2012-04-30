package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class RolesActionMapParameters extends RolesParameterBase {
    private static final long serialVersionUID = 4800306282543787332L;
    private roles_actions privateRoleActionMap;

    public roles_actions getRoleActionMap() {
        return privateRoleActionMap;
    }

    private void setRoleActionMap(roles_actions value) {
        privateRoleActionMap = value;
    }

    public RolesActionMapParameters(roles_actions roleActionMap) {
        super(roleActionMap.getrole_id());
        setRoleActionMap(roleActionMap);
    }

    public RolesActionMapParameters() {
    }
}
