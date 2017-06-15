package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class RolesParameterBase extends ActionParametersBase {
    private static final long serialVersionUID = -1569030140723911754L;

    public RolesParameterBase(Guid roleId) {
        setRoleId(roleId);
    }

    @NotNull
    private Guid privateRoleId;

    public Guid getRoleId() {
        return privateRoleId;
    }

    public void setRoleId(Guid value) {
        privateRoleId = value;
    }

    public RolesParameterBase() {
        privateRoleId = Guid.Empty;
    }
}
