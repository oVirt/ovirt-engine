package org.ovirt.engine.core.common.businessentities.storage;

import org.ovirt.engine.core.common.utils.ObjectUtils;

/**
 * A type of disk that is stored directly on a LUN ({@link LUNs}). This disk will contain the LUN details.
 */
public class LunDisk extends Disk {

    private static final long serialVersionUID = -5177863078960026966L;

    /**
     * The LUN details.
     */
    private LUNs lun;

    private Boolean usingScsiReservation;

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

    public Boolean isUsingScsiReservation() {
        return usingScsiReservation;
    }

    public void setUsingScsiReservation(Boolean value) {
        this.usingScsiReservation = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((usingScsiReservation == null) ? 0 : usingScsiReservation.hashCode());
        result = prime * result + ((lun == null) ? 0 : lun.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LunDisk)) {
            return false;
        }
        LunDisk other = (LunDisk) obj;
        return (ObjectUtils.objectsEqual(lun, other.lun)
                && ObjectUtils.objectsEqual(usingScsiReservation, other.usingScsiReservation));
    }
}
