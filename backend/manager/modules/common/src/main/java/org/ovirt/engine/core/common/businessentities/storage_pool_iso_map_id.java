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
public class storage_pool_iso_map_id implements Serializable {
    private static final long serialVersionUID = -3579958698510291360L;

    @Type(type = "guid")
    Guid storageId;

    @Type(type = "guid")
    NGuid storagePoolId;
}
