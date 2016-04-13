package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ImageActionsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;

    public ImageActionsVDSCommandParameters(Guid vdsId, Guid spId, Guid sdId, Guid imgGroupId, Guid imgId) {
        super(vdsId);
        setStoragePoolId(spId);
        setStorageDomainId(sdId);
        setImageGroupId(imgGroupId);
        setImageId(imgId);
    }

    public ImageActionsVDSCommandParameters() {}

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getImageId() {
        return imageId;
    }
}
