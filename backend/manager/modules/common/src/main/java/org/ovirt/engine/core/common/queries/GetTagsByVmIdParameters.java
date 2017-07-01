package org.ovirt.engine.core.common.queries;

public class GetTagsByVmIdParameters extends QueryParametersBase {
    private static final long serialVersionUID = -8537901288950684062L;

    public GetTagsByVmIdParameters(String vmId) {
        _vmId = vmId;
    }

    private String _vmId;

    public String getVmId() {
        return _vmId;
    }

    public GetTagsByVmIdParameters() {
    }
}
