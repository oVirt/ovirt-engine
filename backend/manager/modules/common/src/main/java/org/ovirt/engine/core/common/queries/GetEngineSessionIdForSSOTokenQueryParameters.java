package org.ovirt.engine.core.common.queries;

public class GetEngineSessionIdForSSOTokenQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -8484542140039444653L;
    private String token;

    public GetEngineSessionIdForSSOTokenQueryParameters() {
    }

    public GetEngineSessionIdForSSOTokenQueryParameters(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
