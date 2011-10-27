package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Serializable;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class vm_template_image_map_id implements Serializable {
    private static final long serialVersionUID = -4485462537553606750L;

    @Type(type = "guid")
    public Guid imageTemplateId;

    @Type(type = "guid")
    public Guid vmTemplateId;
}
