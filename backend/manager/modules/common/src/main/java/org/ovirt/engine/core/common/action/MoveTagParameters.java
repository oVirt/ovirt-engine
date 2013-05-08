package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MoveTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -6320801761505304462L;
    private Guid _newParentId;

    public MoveTagParameters(Guid tagId, Guid newParentId) {
        super(tagId);
        _newParentId = newParentId;
    }

    public Guid getNewParentId() {
        return _newParentId;
    }

    public MoveTagParameters() {
    }
}
