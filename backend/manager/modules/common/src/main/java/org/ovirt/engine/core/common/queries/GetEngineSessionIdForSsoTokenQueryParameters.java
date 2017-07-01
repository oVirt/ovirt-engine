package org.ovirt.engine.core.common.queries;

public class GetEngineSessionIdForSsoTokenQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = -8484542140039444653L;
    private String token;

    public GetEngineSessionIdForSsoTokenQueryParameters() {
    }

    public GetEngineSessionIdForSsoTokenQueryParameters(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
