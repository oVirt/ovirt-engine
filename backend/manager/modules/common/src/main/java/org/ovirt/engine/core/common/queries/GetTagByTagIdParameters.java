package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetTagByTagIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4371399673035908432L;

    public GetTagByTagIdParameters(Guid tagId) {
        _tagId = tagId;
    }

    private Guid _tagId;

    public Guid getTagId() {
        return _tagId;
    }

    public GetTagByTagIdParameters() {
    }
}
