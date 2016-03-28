package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private long imageSizeInBytes;
    private Guid newImageID;
    private Guid srcImageId;
    private Guid srcImageGroupId;
    private Guid newImageGroupId;
    private VolumeFormat volumeFormat;
    private String description;
    private Long initialSize;

    public CreateVolumeVDSCommandParameters(Guid storageDomainId, Guid jobId, long imageSizeInBytes, Guid newImageID,
                                            Guid srcImageId, Guid srcImageGroupId, Guid newImageGroupId, VolumeFormat
                                                    volumeFormat, String description, Long initialSize) {
        super(storageDomainId, jobId);
        this.imageSizeInBytes = imageSizeInBytes;
        this.newImageID = newImageID;
        this.srcImageId = srcImageId;
        this.srcImageGroupId = srcImageGroupId;
        this.newImageGroupId = newImageGroupId;
        this.volumeFormat = volumeFormat;
        this.description = description;
        this.initialSize = initialSize;
    }

    public long getImageSizeInBytes() {
        return imageSizeInBytes;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    protected void setVolumeFormat(VolumeFormat value) {
        volumeFormat = value;
    }

    public Guid getNewImageID() {
        return newImageID;
    }

    protected void setNewImageID(Guid value) {
        newImageID = value;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String value) {
        description = value;
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

    public Guid getSrcImageGroupId() {
        return srcImageGroupId;
    }

    public void setSrcImageGroupId(Guid srcImageGroupId) {
        this.srcImageGroupId = srcImageGroupId;
    }

    public Guid getNewImageGroupId() {
        return newImageGroupId;
    }

    public void setNewImageGroupId(Guid newImageGroupId) {
        this.newImageGroupId = newImageGroupId;
    }

    public Long getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Long initialSize) {
        this.initialSize = initialSize;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("imageSizeInBytes", getImageSizeInBytes())
                .append("volumeFormat", getVolumeFormat())
                .append("srcImageGroupId", getSrcImageGroupId())
                .append("srcImageId", getSrcImageId())
                .append("newImageGroupId", getNewImageGroupId())
                .append("newImageId", getNewImageID())
                .append("description", getDescription())
                .append("initialSize", getInitialSize());
    }

    public CreateVolumeVDSCommandParameters() {
    }
}
