package org.ovirt.engine.core.common.queries;

public class GetTagsByUserGroupIdParameters extends QueryParametersBase {
    private static final long serialVersionUID = 7397977099793878525L;

    public GetTagsByUserGroupIdParameters(String groupId) {
        _groupId = groupId;
    }

    private String _groupId;

    public String getGroupId() {
        return _groupId;
    }

    public GetTagsByUserGroupIdParameters() {
    }
}
