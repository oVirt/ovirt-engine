package org.ovirt.engine.core.common.queries;

public class GetTagsByUserIdParameters extends QueryParametersBase {
    private static final long serialVersionUID = -6658285833163439520L;

    public GetTagsByUserIdParameters(String userId) {
        _userId = userId;
    }

    private String _userId;

    public String getUserId() {
        return _userId;
    }

    public GetTagsByUserIdParameters() {
    }
}
