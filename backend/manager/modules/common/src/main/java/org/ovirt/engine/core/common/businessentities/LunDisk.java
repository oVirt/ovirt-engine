package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

/**
 * A type of disk that is stored directly on a LUN ({@link LUNs}). This disk will contain the LUN details.
 */
public class LunDisk extends Disk {

    private static final long serialVersionUID = -5177863078960026966L;

    /**
     * The LUN details.
     */
    private LUNs lun;

    public LunDisk() {
    }

    public LunDisk(Guid id,
            int internalDriveMapping,
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors,
            VmEntityType vmEntityType,
            String diskAlias,
            String diskDescription,
            boolean shareable,
            boolean boot,
            LUNs lun) {
        super(id,
                internalDriveMapping,
                diskInterface,
                wipeAfterDelete,
                propagateErrors,
                vmEntityType,
                diskAlias,
                diskDescription,
                shareable,
                boot,
                false);
        this.lun = lun;
    }

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
    public long getsize() {
        return lun.getDeviceSize();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        if (lun == null) {
            if (other.lun != null) {
                return false;
            }
        } else if (!lun.equals(other.lun)) {
            return false;
        }
        return true;
    }
}
