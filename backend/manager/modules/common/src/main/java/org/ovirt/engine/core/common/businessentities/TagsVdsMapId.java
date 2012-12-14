package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class TagsVdsMapId implements Serializable {
    private static final long serialVersionUID = -8781389672965898588L;

    @Type(type = "guid")
    Guid tagId;

    @Type(type = "guid")
    Guid vdsId;

    public TagsVdsMapId() {
    }

    public TagsVdsMapId(Guid tagId, Guid vdsId) {
        this.tagId = tagId;
        this.vdsId = vdsId;
    }
}
