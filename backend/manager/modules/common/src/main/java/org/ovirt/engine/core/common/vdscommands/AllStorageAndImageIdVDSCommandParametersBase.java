package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class AllStorageAndImageIdVDSCommandParametersBase extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public AllStorageAndImageIdVDSCommandParametersBase(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageId = imageId;
    }

    private Guid _imageId;

    public Guid getImageId() {
        return _imageId;
    }

    public AllStorageAndImageIdVDSCommandParametersBase() {
        _imageId = Guid.Empty;
    }

    @Override
    public String toString() {
        return String.format("%s, imageId = %s", super.toString(), getImageId());
    }
}
