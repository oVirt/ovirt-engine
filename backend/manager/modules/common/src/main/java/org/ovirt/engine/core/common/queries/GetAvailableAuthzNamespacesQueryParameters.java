package org.ovirt.engine.core.common.queries;

public class GetAvailableAuthzNamespacesQueryParameters extends VdcQueryParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 2473511045533756911L;
    private String authzName;

    public GetAvailableAuthzNamespacesQueryParameters() {
    }

    public GetAvailableAuthzNamespacesQueryParameters(String authzName) {
        this.authzName = authzName;
    }

    public String getAuthzName() {
        return authzName;
    }

}
