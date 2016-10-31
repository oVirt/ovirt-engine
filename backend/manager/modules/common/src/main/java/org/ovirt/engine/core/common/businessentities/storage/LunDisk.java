package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

/**
 * A type of disk that is stored directly on a LUN ({@link LUNs}). This disk will contain the LUN details.
 */
public class LunDisk extends Disk {

    private static final long serialVersionUID = -5177863078960026966L;

    /**
     * The LUN details.
     */
    private LUNs lun;

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.LUN;
    }

    public LUNs getLun() {
        return lun;
    }

    public void setLun(LUNs lun) {
        this.lun = lun;
    }

    @Override
    public long getSize() {
        return lun.getDeviceSize();
    }

    @Override
    public boolean isAllowSnapshot() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lun);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LunDisk)) {
            return false;
        }
        LunDisk other = (LunDisk) obj;
        return super.equals(obj) && Objects.equals(lun, other.lun);
    }
}
