package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CreateVolumeContainerCommandParameters extends StorageJobCommandParameters {
    private long size;
    private VolumeFormat volumeFormat;
    private VolumeType volumeType;
    private Guid srcImageGroupId;
    private Guid srcImageId;
    private Long initialSize;
    private boolean legal = true;

    private Integer sequenceNumber;

    public CreateVolumeContainerCommandParameters() {
    }

    public CreateVolumeContainerCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid srcImageGroupId,
                                                  Guid srcImageId, Guid imageGroupId, Guid imageId,
                                                  VolumeFormat volumeFormat, VolumeType volumeType, String description, long size,
                                                  Long initialSize, Integer sequenceNumber) {
        setStoragePoolId(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
        setImageId(imageId);
        setDescription(description);
        this.srcImageGroupId = srcImageGroupId;
        this.srcImageId = srcImageId;
        this.size = size;
        this.volumeFormat = volumeFormat;
        this.volumeType = volumeType;
        this.initialSize = initialSize;
        this.sequenceNumber = sequenceNumber;
        fillEntityInfo(imageId);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public Guid getSrcImageGroupId() {
        return srcImageGroupId;
    }

    public void setSrcImageGroupId(Guid srcImageGroupId) {
        this.srcImageGroupId = srcImageGroupId;
    }

    public Guid getSrcImageId() {
        return srcImageId;
    }

    public void setSrcImageId(Guid srcImageId) {
        this.srcImageId = srcImageId;
    }

    public Long getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Long initialSize) {
        this.initialSize = initialSize;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public boolean isLegal() {
        return legal;
    }

    public void setLegal(boolean legal) {
        this.legal = legal;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
