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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CinderDisk that = (CinderDisk) o;

        return !(cinderConnectionInfo != null ? !cinderConnectionInfo.equals(that.cinderConnectionInfo) : that.cinderConnectionInfo != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cinderConnectionInfo != null ? cinderConnectionInfo.hashCode() : 0);
        return result;
    }
}
