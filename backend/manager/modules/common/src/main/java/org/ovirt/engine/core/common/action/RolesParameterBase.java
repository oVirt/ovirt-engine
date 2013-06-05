package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class RolesParameterBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -1569030140723911754L;

    public RolesParameterBase(Guid roleId) {
        setRoleId(roleId);
    }

    @NotNull
    private Guid privateRoleId = Guid.Empty;

    public Guid getRoleId() {
        return privateRoleId;
    }

    public void setRoleId(Guid value) {
        privateRoleId = value;
    }

    public RolesParameterBase() {
    }
}
