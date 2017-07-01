package org.ovirt.engine.core.common.queries;

public class GetAgentFenceOptionsQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 3645032828911858219L;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GetAgentFenceOptionsQueryParameters(String version) {
        super();
        this.version = version;
    }

    public GetAgentFenceOptionsQueryParameters() {
    }
}
