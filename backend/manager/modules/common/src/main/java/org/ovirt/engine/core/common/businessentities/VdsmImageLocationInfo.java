package org.ovirt.engine.core.common.businessentities;

import java.util.StringJoiner;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class VdsmImageLocationInfo extends LocationInfo {
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;
    private Integer generation;
    private boolean prepared;

    public VdsmImageLocationInfo(Guid storageDomainId, Guid imageGroupId, Guid imageGuid, Integer generation) {
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageGuid;
        this.generation = generation;
    }

    public VdsmImageLocationInfo(Guid storageDomainId, Guid imageGroupId, Guid imageGuid, Integer generation, boolean prepared) {
        this(storageDomainId, imageGroupId, imageGuid, generation);
        this.prepared = prepared;
    }

    public VdsmImageLocationInfo(DiskImage diskImage) {
        this.storageDomainId = !diskImage.getStorageIds().isEmpty() ? diskImage.getStorageIds().get(0) : null;
        this.imageGroupId = diskImage.getId();
        this.imageId = diskImage.getImageId();
        this.generation = diskImage.getImage().getGeneration();
    }

    public VdsmImageLocationInfo() {
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

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public boolean isPrepared() {
        return prepared;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VdsmImageLocationInfo.class.getSimpleName() + "[", "]")
                .add("storageDomainId=" + storageDomainId)
                .add("imageGroupId=" + imageGroupId)
                .add("imageId=" + imageId)
                .add("generation=" + generation)
                .add("prepared=" + prepared)
                .toString();
    }
}
