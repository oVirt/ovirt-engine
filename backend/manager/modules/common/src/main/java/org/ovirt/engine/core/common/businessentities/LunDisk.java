package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

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
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors,
            VmEntityType vmEntityType,
            int numberOfVms,
            ArrayList<String> vmNames,
            String diskAlias,
            String diskDescription,
            boolean shareable,
            boolean boot,
            LUNs lun) {
        super(id,
                diskInterface,
                wipeAfterDelete,
                propagateErrors,
                vmEntityType,
                numberOfVms,
                vmNames,
                diskAlias,
                diskDescription,
                shareable,
                boot);
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
    public long getSize() {
        return lun.getDeviceSize();
    }

    @Override
    public boolean isAllowSnapshot() {
        return false;
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
