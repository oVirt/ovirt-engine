package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateSnapshotVDSCommandParameters extends CreateImageVDSCommandParameters {
    public CreateSnapshotVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId, long imgSizeInBytes, VolumeType imageType, VolumeFormat volFormat,
            Guid sourceImageGroupId, Guid newImageId, String newImageDescription) {
        super(storagePoolId, storageDomainId, imageGroupId, imgSizeInBytes, imageType, volFormat, newImageId,
                newImageDescription);
        _imageId = imageId;
        setSourceImageGroupId(sourceImageGroupId);
    }

    private Guid _imageId;
    private Guid privateSourceImageGroupId;

    public Guid getImageId() {
        return _imageId;
    }

    public Guid getSourceImageGroupId() {
        return privateSourceImageGroupId;
    }

    public void setSourceImageGroupId(Guid value) {
        privateSourceImageGroupId = value;
    }

    public CreateSnapshotVDSCommandParameters() {
        _imageId = Guid.Empty;
        privateSourceImageGroupId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", getImageId())
                .append("sourceImageGroupId", getSourceImageGroupId());
    }
}
