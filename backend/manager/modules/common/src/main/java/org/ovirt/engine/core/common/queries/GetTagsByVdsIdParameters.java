package org.ovirt.engine.core.common.queries;

public class GetTagsByVdsIdParameters extends QueryParametersBase {
    private static final long serialVersionUID = 2616882989867228100L;

    public GetTagsByVdsIdParameters(String vdsId) {
        _vdsId = vdsId;
    }

    private String _vdsId;

    public String getVdsId() {
        return _vdsId;
    }

    public GetTagsByVdsIdParameters() {
    }
}
