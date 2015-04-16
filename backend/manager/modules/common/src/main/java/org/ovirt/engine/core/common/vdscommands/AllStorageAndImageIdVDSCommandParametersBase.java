package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", getImageId());
    }
}
