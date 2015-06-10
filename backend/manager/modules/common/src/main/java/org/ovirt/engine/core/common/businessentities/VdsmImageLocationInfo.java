package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class VdsmImageLocationInfo extends LocationInfo {
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;

    public VdsmImageLocationInfo(Guid storageDomainId, Guid imageGroupId, Guid imageGuid) {
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageGuid;
    }

    public VdsmImageLocationInfo(DiskImage diskImage) {
        this.storageDomainId = !diskImage.getStorageIds().isEmpty() ? diskImage.getStorageIds().get(0) : null;
        this.imageGroupId = diskImage.getId();
        this.imageId = diskImage.getImageId();
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "VdsmImageLocationInfo [" +
                "storageDomainId=" + storageDomainId +
                ", imageGroupId=" + imageGroupId +
                ", imageId=" + imageId +
                ']';
    }
}
