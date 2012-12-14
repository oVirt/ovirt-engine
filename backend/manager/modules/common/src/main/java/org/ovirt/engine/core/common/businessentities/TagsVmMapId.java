package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class TagsVmMapId implements Serializable {
    private static final long serialVersionUID = 3806639687244222549L;

    @Type(type = "guid")
    Guid tagId;

    @Type(type = "guid")
    Guid vmId;

    public TagsVmMapId() {
    }

    public TagsVmMapId(Guid tagId, Guid vmId) {
        super();
        this.tagId = tagId;
        this.vmId = vmId;
    }
}
