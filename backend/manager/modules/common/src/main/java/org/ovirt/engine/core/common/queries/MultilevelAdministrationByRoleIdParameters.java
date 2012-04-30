package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class MultilevelAdministrationByRoleIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = -6638689960055937254L;

    @NotNull(message = "VALIDATION.ROLES.ID.NOT_NULL")
    private Guid privateRoleId = new Guid();

    public Guid getRoleId() {
        return privateRoleId;
    }

    private void setRoleId(Guid value) {
        privateRoleId = value;
    }

    public MultilevelAdministrationByRoleIdParameters(Guid roleId) {
        setRoleId(roleId);
    }

    public MultilevelAdministrationByRoleIdParameters() {
    }
}
