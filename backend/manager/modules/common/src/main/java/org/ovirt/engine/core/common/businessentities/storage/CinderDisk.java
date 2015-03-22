package org.ovirt.engine.core.common.businessentities.storage;

public class CinderDisk extends DiskImage {

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.CINDER;
    }

    @Override
    public boolean isAllowSnapshot() {
        return false; // todo: implement snapshots support
    }
}
