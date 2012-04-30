package org.ovirt.engine.core.common.queries;

public class MultilevelAdministrationByRoleNameParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = 584607807898620094L;

    private String privateRoleName;

    public String getRoleName() {
        return privateRoleName;
    }

    private void setRoleName(String value) {
        privateRoleName = value;
    }

    public MultilevelAdministrationByRoleNameParameters(String roleName) {
        setRoleName(roleName);
    }

    public MultilevelAdministrationByRoleNameParameters() {
    }
}
