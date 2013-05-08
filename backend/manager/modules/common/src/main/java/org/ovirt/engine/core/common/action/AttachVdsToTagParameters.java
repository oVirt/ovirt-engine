package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class AttachVdsToTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -6599471346607548452L;
    private java.util.ArrayList<Guid> _entitiesId;

    public AttachVdsToTagParameters(Guid tagId, java.util.ArrayList<Guid> entitiesId) {
        super(tagId);
        _entitiesId = entitiesId;
    }

    public java.util.ArrayList<Guid> getEntitiesId() {
        return _entitiesId == null ? new ArrayList<Guid>() : _entitiesId;
    }

    public AttachVdsToTagParameters() {
    }
}
