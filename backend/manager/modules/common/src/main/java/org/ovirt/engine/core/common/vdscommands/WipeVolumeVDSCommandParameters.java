package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class WipeVolumeVDSCommandParameters extends StorageDomainVdsCommandParameters {
    private Guid imageGroupId;
    private Guid imageId;

    public WipeVolumeVDSCommandParameters() {
    }

    public WipeVolumeVDSCommandParameters(Guid storageDomainId, Guid vdsId, Guid imageGroupId, Guid imageId) {
        super(storageDomainId, vdsId);
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }
}
