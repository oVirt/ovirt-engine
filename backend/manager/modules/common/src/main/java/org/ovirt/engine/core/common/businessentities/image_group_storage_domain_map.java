package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.*;

@Entity
@Table(name = "image_group_storage_domain_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class image_group_storage_domain_map implements Serializable {
    private static final long serialVersionUID = 8459502119344718863L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "imageGroupId", column = @Column(name = "image_group_id")),
            @AttributeOverride(name = "storageDomainId", column = @Column(name = "storage_domain_id")) })
    private image_group_storage_domain_map_id id = new image_group_storage_domain_map_id();


    public image_group_storage_domain_map() {
    }

    public image_group_storage_domain_map(Guid image_group_id, Guid storage_domain_id) {
        this.id.imageGroupId = image_group_id;
        this.id.storageDomainId = storage_domain_id;
    }

    public Guid getstorage_domain_id() {
        return this.id.storageDomainId;
    }

    public void setstorage_domain_id(Guid value) {
        this.id.storageDomainId = value;
    }

    public Guid getimage_group_id() {
        return this.id.imageGroupId;
    }

    public void setimage_group_id(Guid value) {
        this.id.imageGroupId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.imageGroupId == null) ? 0 : id.imageGroupId.hashCode());
        result = prime * result + ((id.storageDomainId == null) ? 0 : id.storageDomainId.hashCode());
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
        image_group_storage_domain_map other = (image_group_storage_domain_map) obj;
        if (id.imageGroupId == null) {
            if (other.id.imageGroupId != null)
                return false;
        } else if (!id.imageGroupId.equals(other.id.imageGroupId))
            return false;
        if (id.storageDomainId == null) {
            if (other.id.storageDomainId != null)
                return false;
        } else if (!id.storageDomainId.equals(other.id.storageDomainId))
            return false;
        return true;
    }
}
