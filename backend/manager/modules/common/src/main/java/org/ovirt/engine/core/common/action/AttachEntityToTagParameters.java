package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class AttachEntityToTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -180068487863209744L;
    private java.util.ArrayList<Guid> _entitiesId;

    public AttachEntityToTagParameters(Guid tagId, java.util.ArrayList<Guid> entitiesId) {
        super(tagId);
        _entitiesId = entitiesId;
    }

    public java.util.ArrayList<Guid> getEntitiesId() {
        return _entitiesId == null ? new ArrayList<Guid>() : _entitiesId;
    }

    public AttachEntityToTagParameters() {
    }
}
