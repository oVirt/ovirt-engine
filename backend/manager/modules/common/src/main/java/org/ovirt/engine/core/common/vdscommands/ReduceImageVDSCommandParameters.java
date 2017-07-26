package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ReduceImageVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    private Guid imageId;
    private boolean allowActive;

    public ReduceImageVDSCommandParameters() {
    }

    public ReduceImageVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            boolean allowActive) {
        super(storagePoolId, storageDomainId, imageGroupId);
        this.imageId = imageId;
        this.allowActive = allowActive;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public boolean isAllowActive() {
        return allowActive;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", getImageId())
                .append("allowActive", isAllowActive());
    }
}
