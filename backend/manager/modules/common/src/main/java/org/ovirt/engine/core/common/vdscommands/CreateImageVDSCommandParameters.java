package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class CreateImageVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public CreateImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            long imageSizeInBytes, VolumeType imageType, VolumeFormat volFormat, Guid newImageId,
            String newImageDescription, Version poolCompatibilityVersion, DiskContentType diskContentType) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageSizeInBytes = imageSizeInBytes;
        _imageType = imageType;
        this.setVolumeFormat(volFormat);
        setNewImageID(newImageId);
        setNewImageDescription(newImageDescription);
        setPoolCompatibilityVersion(poolCompatibilityVersion);
        setDiskContentType(diskContentType);
    }

    private Version poolCompatibilityVersion;
    private DiskContentType diskContentType;
    private long _imageSizeInBytes;
    private VolumeType _imageType;
    private long imageInitialSizeInBytes;

    public long getImageSizeInBytes() {
        return _imageSizeInBytes;
    }

    public VolumeType getImageType() {
        return _imageType;
    }

    public long getImageInitialSizeInBytes() {
        return imageInitialSizeInBytes;
    }

    public void setImageInitialSizeInBytes(long imageInitialSizeInBytes) {
        this.imageInitialSizeInBytes = imageInitialSizeInBytes;
    }

    private VolumeFormat privateVolumeFormat;

    public VolumeFormat getVolumeFormat() {
        return privateVolumeFormat;
    }

    protected void setVolumeFormat(VolumeFormat value) {
        privateVolumeFormat = value;
    }

    private Guid privateNewImageID;

    public Guid getNewImageID() {
        return privateNewImageID;
    }

    protected void setNewImageID(Guid value) {
        privateNewImageID = value;
    }

    private String privateNewImageDescription;

    public String getNewImageDescription() {
        return privateNewImageDescription;
    }

    protected void setNewImageDescription(String value) {
        privateNewImageDescription = value;
    }

    public Version getPoolCompatibilityVersion() {
        return poolCompatibilityVersion;
    }

    public void setPoolCompatibilityVersion(Version poolCompatibilityVersion) {
        this.poolCompatibilityVersion = poolCompatibilityVersion;
    }

    public DiskContentType getDiskContentType() {
        return diskContentType;
    }

    public void setDiskContentType(DiskContentType diskContentType) {
        this.diskContentType = diskContentType;
    }

    public CreateImageVDSCommandParameters() {
        _imageType = VolumeType.Unassigned;
        privateVolumeFormat = VolumeFormat.UNUSED0;
        privateNewImageID = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageSizeInBytes", getImageSizeInBytes())
                .append("volumeFormat", getVolumeFormat())
                .append("newImageId", getNewImageID())
                .append("imageType", getImageType())
                .append("newImageDescription", getNewImageDescription())
                .append("imageInitialSizeInBytes", getImageInitialSizeInBytes());
    }
}
