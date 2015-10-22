package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RefreshVolumeVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;

    public RefreshVolumeVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId) {
        super(vdsId);
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
    }

    public RefreshVolumeVDSCommandParameters() {}

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
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

    @Override
    public String toString() {
        return new StringBuilder()
                .append(super.toString())
                .append(", storagePoolId = ").append(getStoragePoolId())
                .append(", storageDomainId = ").append(getStorageDomainId())
                .append(", imageGroupId = ").append(getImageGroupId())
                .append(", imageId = ").append(getImageId())
                .toString();
    }
}
