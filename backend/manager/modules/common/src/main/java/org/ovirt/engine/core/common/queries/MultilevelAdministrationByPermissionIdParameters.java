package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class MultilevelAdministrationByPermissionIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = -2853514093532756677L;

    private Guid privatePermissionId = new Guid();

    public Guid getPermissionId() {
        return privatePermissionId;
    }

    private void setPermissionId(Guid value) {
        privatePermissionId = value;
    }

    public MultilevelAdministrationByPermissionIdParameters(Guid permissionId) {
        setPermissionId(permissionId);
    }

    public MultilevelAdministrationByPermissionIdParameters() {
    }
}
