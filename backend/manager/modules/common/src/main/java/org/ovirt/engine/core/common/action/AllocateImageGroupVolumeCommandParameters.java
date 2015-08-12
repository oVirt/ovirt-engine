package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AllocateImageGroupVolumeCommandParameters extends StorageJobCommandParameters {
    private long size;

    public AllocateImageGroupVolumeCommandParameters() {
    }

    public AllocateImageGroupVolumeCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId, long size) {
        setStoragePoolId(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
        setImageId(imageId);
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
