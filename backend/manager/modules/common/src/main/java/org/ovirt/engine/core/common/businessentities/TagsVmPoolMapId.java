package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class TagsVmPoolMapId implements Serializable {
    private static final long serialVersionUID = 2342243725530378982L;

    Guid tagId;
    NGuid vmPoolId;

    public TagsVmPoolMapId() {
    }

    public TagsVmPoolMapId(Guid tagId, Guid vmPoolId) {
        super();
        this.tagId = tagId;
        this.vmPoolId = vmPoolId;
    }
}
