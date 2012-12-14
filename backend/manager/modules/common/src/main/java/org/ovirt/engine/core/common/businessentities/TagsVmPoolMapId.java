package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class TagsVmPoolMapId implements Serializable {
    private static final long serialVersionUID = 2342243725530378982L;

    @Type(type = "guid")
    Guid tagId;

    @Type(type = "guid")
    NGuid vmPoolId;

    public TagsVmPoolMapId() {
    }

    public TagsVmPoolMapId(Guid tagId, Guid vmPoolId) {
        super();
        this.tagId = tagId;
        this.vmPoolId = vmPoolId;
    }
}
