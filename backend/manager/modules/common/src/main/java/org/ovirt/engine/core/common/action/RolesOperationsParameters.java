package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.roles;

public class RolesOperationsParameters extends RolesParameterBase {
    private static final long serialVersionUID = -1785886308686587013L;
    @Valid
    private roles privateRole;

    public roles getRole() {
        return privateRole;
    }

    public void setRole(roles value) {
        privateRole = value;
    }

    public RolesOperationsParameters(roles role) {
        super(role.getId());
        setRole(role);
    }

    public RolesOperationsParameters() {
    }
}
