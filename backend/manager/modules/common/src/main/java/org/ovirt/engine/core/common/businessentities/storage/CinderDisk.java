package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

@Deprecated
public class CinderDisk extends DiskImage {

    CinderConnectionInfo cinderConnectionInfo;

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.CINDER;
    }

    public CinderConnectionInfo getCinderConnectionInfo() {
        return cinderConnectionInfo;
    }

    public void setCinderConnectionInfo(CinderConnectionInfo cinderConnectionInfo) {
        this.cinderConnectionInfo = cinderConnectionInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CinderDisk)) {
            return false;
        }
        CinderDisk other = (CinderDisk) obj;
        return super.equals(obj)
                && Objects.equals(cinderConnectionInfo, other.cinderConnectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                cinderConnectionInfo
        );
    }

    @Override
    public boolean hasActualSize() {
        // not applicable for Cinder disks
        return false;
    }
}
