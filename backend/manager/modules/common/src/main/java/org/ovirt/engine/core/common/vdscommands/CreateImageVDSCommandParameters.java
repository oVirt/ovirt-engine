package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CreateImageVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public CreateImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            long imageSizeInBytes, VolumeType imageType, VolumeFormat volFormat, Guid newImageId,
            String newImageDescription, String competabilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageSizeInBytes = imageSizeInBytes;
        _imageType = imageType;
        this.setVolumeFormat(volFormat);
        setNewImageID(newImageId);
        setNewImageDescription(newImageDescription);
        setCompatibilityVersion(competabilityVersion);
    }

    private long _imageSizeInBytes;
    private VolumeType _imageType = VolumeType.forValue(0);

    public long getImageSizeInBytes() {
        return _imageSizeInBytes;
    }

    public VolumeType getImageType() {
        return _imageType;
    }

    private VolumeFormat privateVolumeFormat = VolumeFormat.forValue(0);

    public VolumeFormat getVolumeFormat() {
        return privateVolumeFormat;
    }

    protected void setVolumeFormat(VolumeFormat value) {
        privateVolumeFormat = value;
    }

    private Guid privateNewImageID = new Guid();

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

    public CreateImageVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageSizeInBytes = %s, volumeFormat = %s, newImageId = %s, " +
                "newImageDescription = %s",
                super.toString(),
                getImageSizeInBytes(),
                getVolumeFormat(),
                getNewImageID(),
                getNewImageDescription());
    }
}
