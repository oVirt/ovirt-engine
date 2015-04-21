package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;

public class CreateVolumeVDSCommandParameters extends StorageDomainVdsCommandParameters {

    private long imageSizeInBytes;
    private Guid newImageID;
    private Guid srcImageId;
    private Guid srcImageGroupId;
    private Guid newImageGroupId;
    private VolumeFormat volumeFormat;
    private String description;

    public CreateVolumeVDSCommandParameters(Guid storageDomainId, Guid newImageGroupId,
            long imageSizeInBytes, VolumeFormat volFormat, Guid newImageId,
            String newImageDescription, Guid srcImageGroupId, Guid srcImageId) {
        super(storageDomainId);
        setImageSizeInBytes(imageSizeInBytes);
        setVolumeFormat(volFormat);
        setNewImageGroupId(newImageGroupId);
        setNewImageID(newImageId);
        setDescription(newImageDescription);
        setSrcImageGroupId(srcImageGroupId);
        setSrcImageId(srcImageId);
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

    @Override
    public String toString() {
        return String.format("%s, imageSizeInBytes = %s, volumeFormat = %s, srcImageGroupId = %s, srcImageId = %s," +
                " newImageGroupId = %s, newImageId = %s, description = %s",
                super.toString(),
                getImageSizeInBytes(),
                getVolumeFormat(),
                getSrcImageGroupId(),
                getSrcImageId(),
                getNewImageGroupId(),
                getNewImageID(),
                getDescription());
    }

    public CreateVolumeVDSCommandParameters() {
    }
}
