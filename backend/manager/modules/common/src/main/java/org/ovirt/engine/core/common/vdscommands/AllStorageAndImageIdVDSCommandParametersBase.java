package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class AllStorageAndImageIdVDSCommandParametersBase extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public AllStorageAndImageIdVDSCommandParametersBase(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageId = imageId;
    }

    private Guid _imageId = Guid.Empty;

    public Guid getImageId() {
        return _imageId;
    }

    public AllStorageAndImageIdVDSCommandParametersBase() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageId = %s", super.toString(), getImageId());
    }
}
