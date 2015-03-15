package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class DiskLunMapId implements Serializable {

    private static final long serialVersionUID = -5801053874071610531L;

    private Guid diskId;

    private String lunId;

    public DiskLunMapId() {
    }

    public DiskLunMapId(Guid diskId, String lunId) {
        this.diskId = diskId;
        this.lunId = lunId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public String getLunId() {
        return lunId;
    }

    public void setLunId(String lunId) {
        this.lunId = lunId;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + ((diskId == null) ? 0 : diskId.hashCode());
        result = prime * result + ((lunId == null) ? 0 : lunId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DiskLunMapId other = (DiskLunMapId) obj;
        return (ObjectUtils.objectsEqual(diskId, other.diskId)
                && ObjectUtils.objectsEqual(lunId, other.lunId));
    }

}
