package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class VmReplicateDiskParameters extends VdsAndVmIDVDSParametersBase {

    public VmReplicateDiskParameters(Guid vdsId,
            Guid vmId,
            Guid storagePoolId,
            Guid srcStorageDomainId,
            Guid targetStorageDomainId,
            Guid imageGroupId,
            Guid imageId) {
        super(vdsId, vmId);
        this.storagePoolId = storagePoolId;
        this.srcStorageDomainId = srcStorageDomainId;
        this.targetStorageDomainId = targetStorageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
    }

    public VmReplicateDiskParameters() {
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getSrcStorageDomainId() {
        return srcStorageDomainId;
    }

    public void setSrcStorageDomainId(Guid srcStorageDomainId) {
        this.srcStorageDomainId = srcStorageDomainId;
    }

    public Guid getTargetStorageDomainId() {
        return targetStorageDomainId;
    }

    public void setTargetStorageDomainId(Guid targetStorageDomainId) {
        this.targetStorageDomainId = targetStorageDomainId;
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

    private Guid storagePoolId;
    private Guid srcStorageDomainId;
    private Guid targetStorageDomainId;
    private Guid imageGroupId;
    private Guid imageId;

}
