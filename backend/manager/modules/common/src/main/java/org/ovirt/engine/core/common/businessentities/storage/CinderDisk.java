package org.ovirt.engine.core.common.businessentities.storage;

public class CinderDisk extends DiskImage {

    CinderConnectionInfo cinderConnectionInfo;

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.CINDER;
    }

    @Override
    public boolean isAllowSnapshot() {
        return false; // todo: implement snapshots support
    }

    public CinderConnectionInfo getCinderConnectionInfo() {
        return cinderConnectionInfo;
    }

    public void setCinderConnectionInfo(CinderConnectionInfo cinderConnectionInfo) {
        this.cinderConnectionInfo = cinderConnectionInfo;
    }
}
