package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SubchainInfo {

    private Guid storageDomainId;
    private Integer baseImageGeneration;
    private DiskImage baseImage;
    private DiskImage topImage;

    public SubchainInfo() {
    }

    public SubchainInfo(DiskImage baseImage, DiskImage topImage) {
        this.storageDomainId = baseImage.getStorageIds().get(0);
        this.baseImage = baseImage;
        this.topImage = topImage;
    }

    public SubchainInfo(Guid storageDomainId, DiskImage baseImage, DiskImage topImage) {
        this.storageDomainId = storageDomainId;
        this.baseImage = baseImage;
        this.topImage = topImage;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public Guid getImageGroupId() {
        return baseImage.getId();
    }

    public Guid getBaseImageId() {
        return getBaseImage().getImageId();
    }

    public Guid getTopImageId() {
        return getTopImage().getImageId();
    }

    public Integer getBaseImageGeneration() {
        return baseImageGeneration;
    }

    public void setBaseImageGeneration(Integer generation) {
        this.baseImageGeneration = generation;
    }

    public DiskImage getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(DiskImage baseImage) {
        this.baseImage = baseImage;
    }

    public DiskImage getTopImage() {
        return topImage;
    }

    public void setTopImage(DiskImage topImage) {
        this.topImage = topImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubchainInfo that = (SubchainInfo) o;
        return Objects.equals(storageDomainId, that.storageDomainId) &&
                Objects.equals(baseImageGeneration, that.baseImageGeneration) &&
                Objects.equals(baseImage, that.baseImage) &&
                Objects.equals(topImage, that.topImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageDomainId,
                baseImageGeneration,
                baseImage,
                topImage);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("storageDomainId", storageDomainId)
                .append("baseImage", baseImage)
                .append("topImage", topImage)
                .append("baseImageGeneration", baseImageGeneration)
                .build();
    }
}
