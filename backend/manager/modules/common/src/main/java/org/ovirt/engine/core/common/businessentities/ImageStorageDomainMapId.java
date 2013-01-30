package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class ImageStorageDomainMapId implements Serializable {
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
        ImageStorageDomainMapId other = (ImageStorageDomainMapId) obj;
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
