package org.ovirt.engine.core.common.queries;

public class GetSupportedCpuListParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -6961495931946670043L;

    public GetSupportedCpuListParameters(String maxCpuName) {
        _maxCpuName = maxCpuName;
    }

    private String _maxCpuName;

    public String getMaxCpuName() {
        return _maxCpuName;
    }

    public GetSupportedCpuListParameters() {
    }
}
