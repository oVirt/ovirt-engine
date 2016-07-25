package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CreateVolumeParameters extends ImagesActionsParametersBase {
    private long imageSizeInBytes;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid newImageId;
    private Guid srcImageId;
    private Guid srcImageGroupId;
    private Guid newImageGroupId;
    private VolumeFormat volumeFormat;
    private String description;
    private VolumeType volumeType;
    private CreationState creationState = CreationState.VOLUME_CREATION;
    private Long initialSize;

    public CreateVolumeParameters(Guid storagePoolId, Guid storageDomainId, Guid newImageGroupId, Guid newImageId,
                                  Guid srcImageGroupId, Guid srcImageId,
                                  long imageSizeInBytes, Long initialSize, VolumeFormat volumeFormat, VolumeType volumeType,
                                  String description) {
        this.imageSizeInBytes = imageSizeInBytes;
        this.newImageId = newImageId;
        this.srcImageGroupId = srcImageGroupId;
        this.newImageGroupId = newImageGroupId;
        this.volumeFormat = volumeFormat;
        this.description = description;
        this.volumeType = volumeType;
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageSizeInBytes = imageSizeInBytes;
        this.initialSize = initialSize;
        this.srcImageId = srcImageId;
    }


    public CreateVolumeParameters() {
    }

    public long getImageSizeInBytes() {
        return imageSizeInBytes;
    }

    public void setImageSizeInBytes(long imageSizeInBytes) {
        this.imageSizeInBytes = imageSizeInBytes;
    }

    public Guid getSrcImageId() {
        return srcImageId;
    }

    public void setSrcImageId(Guid srcImageId) {
        this.srcImageId = srcImageId;
    }

    public Guid getNewImageGroupId() {
        return newImageGroupId;
    }

    public void setNewImageGroupId(Guid newImageGroupId) {
        this.newImageGroupId = newImageGroupId;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public CreationState getCreationState() {
        return creationState;
    }

    public void setCreationState(CreationState creationState) {
        this.creationState = creationState;
    }

    public enum CreationState {
        VOLUME_CREATION, VOLUME_ALLOCATION;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getNewImageId() {
        return newImageId;
    }

    public void setNewImageId(Guid newImageId) {
        this.newImageId = newImageId;
    }

    public Guid getSrcImageGroupId() {
        return srcImageGroupId;
    }

    public void setSrcImageGroupId(Guid srcImageGroupId) {
        this.srcImageGroupId = srcImageGroupId;
    }

    public Long getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Long initialSize) {
        this.initialSize = initialSize;
    }
}
