package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class image_storage_domain_map implements BusinessEntity<ImageStorageDomainMapId> {
    private static final long serialVersionUID = 8459502119344718863L;

    private ImageStorageDomainMapId id;
    private Guid quotaId;
    private Guid diskProfileId;

    public image_storage_domain_map() {
        id = new ImageStorageDomainMapId();
    }

    public image_storage_domain_map(Guid image_id, Guid storage_domain_id, Guid quotaId, Guid diskProfileId) {
        this();
        this.id.setImageId(image_id);
        this.id.setStorageDomainId(storage_domain_id);
        this.quotaId = quotaId;
        this.diskProfileId = diskProfileId;
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

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getDiskProfileId() {
        return diskProfileId;
    }

    public void setDiskProfileId(Guid diskProfileId) {
        this.diskProfileId = diskProfileId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + ((diskProfileId == null) ? 0 : diskProfileId.hashCode());
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
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(quotaId, other.quotaId)
                && ObjectUtils.objectsEqual(diskProfileId, other.diskProfileId));
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
