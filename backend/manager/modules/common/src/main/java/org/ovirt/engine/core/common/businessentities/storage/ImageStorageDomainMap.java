package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMap implements BusinessEntity<ImageStorageDomainMapId> {
    private static final long serialVersionUID = 8459502119344718863L;

    private ImageStorageDomainMapId id;
    private Guid quotaId;
    private Guid diskProfileId;

    public ImageStorageDomainMap() {
        id = new ImageStorageDomainMapId();
    }

    public ImageStorageDomainMap(Guid image_id, Guid storage_domain_id, Guid quotaId, Guid diskProfileId) {
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
        return Objects.hash(
                id,
                quotaId,
                diskProfileId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImageStorageDomainMap)) {
            return false;
        }
        ImageStorageDomainMap other = (ImageStorageDomainMap) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(quotaId, other.quotaId)
                && Objects.equals(diskProfileId, other.diskProfileId);
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
