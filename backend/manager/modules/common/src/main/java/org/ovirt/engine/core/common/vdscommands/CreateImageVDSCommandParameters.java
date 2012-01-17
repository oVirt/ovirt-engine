package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateImageVDSCommandParameters")
public class CreateImageVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public CreateImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            long imageSizeInBytes, VolumeType imageType, VolumeFormat volFormat, DiskType diskType, Guid newImageId,
            String newImageDescription, String competabilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageSizeInBytes = imageSizeInBytes;
        _imageType = imageType;
        this.setVolumeFormat(volFormat);
        this.setDiskType(diskType);
        setNewImageID(newImageId);
        setNewImageDescription(newImageDescription);
        setCompatibilityVersion(competabilityVersion);
    }

    @XmlElement
    private long _imageSizeInBytes;
    @XmlElement
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

    private DiskType privateDiskType = DiskType.forValue(0);

    public DiskType getDiskType() {
        return privateDiskType;
    }

    protected void setDiskType(DiskType value) {
        privateDiskType = value;
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
        return String.format("%s, imageSizeInBytes = %s, volumeFormat = %s, diskType = %s, newImageId = %s, " +
                "newImageDescription = %s",
                super.toString(),
                getImageSizeInBytes(),
                getVolumeFormat(),
                getDiskType(),
                getNewImageID(),
                getNewImageDescription());
    }
}
