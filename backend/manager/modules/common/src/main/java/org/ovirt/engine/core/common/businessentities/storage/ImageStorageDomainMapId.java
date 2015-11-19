package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMapId implements Serializable {
    @Override
    public int hashCode() {
        return Objects.hash(
                imageId,
                storageDomainId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImageStorageDomainMapId)) {
            return false;
        }
        ImageStorageDomainMapId other = (ImageStorageDomainMapId) obj;
        return Objects.equals(imageId, other.imageId)
                && Objects.equals(storageDomainId, other.storageDomainId);
    }

    private static final long serialVersionUID = -5870880575903017188L;

    private Guid storageDomainId;

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

    public ImageStorageDomainMapId() {
    }

    public ImageStorageDomainMapId(Guid imageId, Guid storageDomainId) {
        this.imageId = imageId;
        this.storageDomainId = storageDomainId;
    }
}
