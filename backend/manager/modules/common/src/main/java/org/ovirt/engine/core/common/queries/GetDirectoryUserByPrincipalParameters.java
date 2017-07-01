package org.ovirt.engine.core.common.queries;

public class GetDirectoryUserByPrincipalParameters extends QueryParametersBase {

    private static final long serialVersionUID = 4178594464331010016L;

    private String principal;
    private String authz;

    public GetDirectoryUserByPrincipalParameters() {
    }

    public GetDirectoryUserByPrincipalParameters(String authz, String principal) {
        this.principal = principal;
        this.authz = authz;
    }

    public String getPrincnipal() {
        return principal;
    }

    public String getAuthz() {
        return authz;
    }

}
