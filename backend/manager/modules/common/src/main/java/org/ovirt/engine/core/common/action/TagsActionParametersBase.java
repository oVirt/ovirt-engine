package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class TagsActionParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = -799396982675260518L;
    private Guid _tagId;

    public TagsActionParametersBase(Guid tagId) {
        _tagId = tagId;
    }

    public Guid getTagId() {
        return _tagId;
    }

    public TagsActionParametersBase() {
    }
}
