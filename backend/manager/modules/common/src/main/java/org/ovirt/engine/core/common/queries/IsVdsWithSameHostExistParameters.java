package org.ovirt.engine.core.common.queries;

public class IsVdsWithSameHostExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8539975719107135326L;

    public IsVdsWithSameHostExistParameters(String hostName) {
        _hostName = hostName;
    }

    private String _hostName;

    public String getHostName() {
        return _hostName;
    }

    public IsVdsWithSameHostExistParameters() {
    }
}
