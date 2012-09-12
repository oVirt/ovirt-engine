package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Role;

public class RolesOperationsParameters extends RolesParameterBase {
    private static final long serialVersionUID = -1785886308686587013L;
    @Valid
    private Role privateRole;

    public Role getRole() {
        return privateRole;
    }

    public void setRole(Role value) {
        privateRole = value;
    }

    public RolesOperationsParameters(Role role) {
        super(role.getId());
        setRole(role);
    }

    public RolesOperationsParameters() {
    }
}
