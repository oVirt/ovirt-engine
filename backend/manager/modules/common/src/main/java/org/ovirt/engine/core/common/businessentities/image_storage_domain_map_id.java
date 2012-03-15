package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Serializable;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class image_storage_domain_map_id implements Serializable {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        image_storage_domain_map_id other = (image_storage_domain_map_id) obj;
        if (imageId == null) {
            if (other.imageId != null)
                return false;
        } else if (!imageId.equals(other.imageId))
            return false;
        if (storageDomainId == null) {
            if (other.storageDomainId != null)
                return false;
        } else if (!storageDomainId.equals(other.storageDomainId))
            return false;
        return true;
    }

    private static final long serialVersionUID = -5870880575903017188L;

    @Type(type = "guid")
    private Guid storageDomainId;

    @Type(type = "guid")
    private Guid imageId;

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public image_storage_domain_map_id() {
    }

    public image_storage_domain_map_id(Guid imageId, Guid storageDomainId) {
        this.imageId = imageId;
        this.storageDomainId = storageDomainId;
    }
}
