package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class CreateVolumeVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {

    private Version poolCompatibilityVersion;
    private DiskContentType diskContentType;
    private long _imageSizeInBytes;
    private VolumeType _imageType;
    private long imageInitialSizeInBytes;
    private boolean shouldAddBitmaps;

    // Initialize with Guid.Empty for creating a new image.
    private Guid _imageId;
    private Guid privateSourceImageGroupId;

    public CreateVolumeVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            long imageSizeInBytes,
            VolumeType imageType,
            VolumeFormat volFormat,
            Guid sourceImageGroupId,
            Guid newImageId,
            String newImageDescription,
            Version compatibilityVersion,
            DiskContentType diskContentType) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageSizeInBytes = imageSizeInBytes;
        _imageType = imageType;
        this.setVolumeFormat(volFormat);
        setNewImageID(newImageId);
        setNewImageDescription(newImageDescription);
        setPoolCompatibilityVersion(compatibilityVersion);
        setDiskContentType(diskContentType);
        _imageId = imageId;
        setSourceImageGroupId(sourceImageGroupId);
    }

    public CreateVolumeVDSCommandParameters() {
        _imageType = VolumeType.Unassigned;
        privateVolumeFormat = VolumeFormat.UNUSED0;
        privateNewImageID = Guid.Empty;
        _imageId = Guid.Empty;
        privateSourceImageGroupId = Guid.Empty;
    }

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

    public Guid getImageId() {
        return _imageId;
    }

    public Guid getSourceImageGroupId() {
        return privateSourceImageGroupId;
    }

    public void setSourceImageGroupId(Guid value) {
        privateSourceImageGroupId = value;
    }

    public boolean shouldAddBitmaps() {
        return shouldAddBitmaps;
    }

    public void setShouldAddBitmaps(boolean shouldAddBitmaps) {
        this.shouldAddBitmaps = shouldAddBitmaps;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageSizeInBytes", getImageSizeInBytes())
                .append("volumeFormat", getVolumeFormat())
                .append("newImageId", getNewImageID())
                .append("imageType", getImageType())
                .append("newImageDescription", getNewImageDescription())
                .append("imageInitialSizeInBytes", getImageInitialSizeInBytes())
                .append("imageId", getImageId())
                .append("sourceImageGroupId", getSourceImageGroupId())
                .append("shouldAddBitmaps", shouldAddBitmaps());
    }
}
