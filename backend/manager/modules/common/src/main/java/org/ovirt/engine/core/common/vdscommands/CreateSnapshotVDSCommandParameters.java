package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateSnapshotVDSCommandParameters")
public class CreateSnapshotVDSCommandParameters extends CreateImageVDSCommandParameters {
    public CreateSnapshotVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId, long imgSizeInBytes, VolumeType imageType, VolumeFormat volFormat, DiskType diskType,
            Guid sourceImageGroupId, Guid newImageId, String newImageDescription, String competabilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId, imgSizeInBytes, imageType, volFormat, diskType, newImageId,
                newImageDescription, competabilityVersion);
        _imageId = imageId;
        setSourceImageGroupId(sourceImageGroupId);
    }

    @XmlElement
    private Guid _imageId = new Guid();

    public Guid getImageId() {
        return _imageId;
    }

    @XmlElement(name = "SourceImageGroupId")
    private Guid privateSourceImageGroupId = new Guid();

    public Guid getSourceImageGroupId() {
        return privateSourceImageGroupId;
    }

    public void setSourceImageGroupId(Guid value) {
        privateSourceImageGroupId = value;
    }

    public CreateSnapshotVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageId = %s, sourceImageGroupId = %s",
                super.toString(),
                getImageId(),
                getSourceImageGroupId());
    }
}
