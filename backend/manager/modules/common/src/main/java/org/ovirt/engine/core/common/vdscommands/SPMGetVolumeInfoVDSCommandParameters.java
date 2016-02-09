package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SPMGetVolumeInfoVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    private Guid imageId;

    public SPMGetVolumeInfoVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
                                                Guid imageGroupId, Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId);
        this.imageId = imageId;
    }

    public SPMGetVolumeInfoVDSCommandParameters() {}

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", getImageId());
    }
}
