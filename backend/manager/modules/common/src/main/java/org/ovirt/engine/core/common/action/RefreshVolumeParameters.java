package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RefreshVolumeParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = 7848350073743441870L;
    private Guid imageGroupId;
    private Guid imageId;

    public RefreshVolumeParameters() {
        this.imageGroupId = Guid.Empty;
        this.imageId = Guid.Empty;
    }

    public RefreshVolumeParameters(Guid vdsId, Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId) {
        super(storagePoolId, storageDomainId);
        setVdsId(vdsId);
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
