package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class TagsVdsMapId implements Serializable {
    private static final long serialVersionUID = -8781389672965898588L;

    Guid tagId;
    Guid vdsId;

    public TagsVdsMapId() {
    }

    public TagsVdsMapId(Guid tagId, Guid vdsId) {
        this.tagId = tagId;
        this.vdsId = vdsId;
    }
}
