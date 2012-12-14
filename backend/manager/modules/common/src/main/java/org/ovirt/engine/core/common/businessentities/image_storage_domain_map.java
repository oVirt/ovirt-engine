package org.ovirt.engine.core.common.businessentities;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "image_storage_domain_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class image_storage_domain_map implements BusinessEntity<ImageStorageDomainMapId> {
    private static final long serialVersionUID = 8459502119344718863L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "imageId", column = @Column(name = "image_id")),
            @AttributeOverride(name = "storageDomainId", column = @Column(name = "storage_domain_id")) })
    private ImageStorageDomainMapId id = new ImageStorageDomainMapId();


    public image_storage_domain_map() {
    }

    public image_storage_domain_map(Guid image_id, Guid storage_domain_id) {
        this.id.setImageId(image_id);
        this.id.setStorageDomainId(storage_domain_id);
    }

    public Guid getstorage_domain_id() {
        return this.id.getStorageDomainId();
    }

    public void setstorage_domain_id(Guid value) {
        this.id.setStorageDomainId(value);
    }

    public Guid getimage_id() {
        return this.id.getImageId();
    }

    public void setimage_id(Guid value) {
        this.id.setImageId(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof image_storage_domain_map)) {
            return false;
        }
        image_storage_domain_map other = (image_storage_domain_map) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public ImageStorageDomainMapId getId() {
        return id;
    }

    @Override
    public void setId(ImageStorageDomainMapId id) {
        this.id = id;
    }
}
