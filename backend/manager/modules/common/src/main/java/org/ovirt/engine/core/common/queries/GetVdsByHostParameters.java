package org.ovirt.engine.core.common.queries;

public class GetVdsByHostParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3121291409184710688L;

    public GetVdsByHostParameters(String hostname) {
        _hostName = hostname;
    }

    private String _hostName;

    public String getHostName() {
        return _hostName;
    }

    public GetVdsByHostParameters() {
    }
}
