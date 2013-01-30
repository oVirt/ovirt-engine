package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class TagsVmMapId implements Serializable {
    private static final long serialVersionUID = 3806639687244222549L;

    Guid tagId;
    Guid vmId;

    public TagsVmMapId() {
    }

    public TagsVmMapId(Guid tagId, Guid vmId) {
        super();
        this.tagId = tagId;
        this.vmId = vmId;
    }
}
