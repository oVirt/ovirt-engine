package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Objects;

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
        return Objects.hash(
                diskId,
                lunId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiskLunMapId)) {
            return false;
        }
        DiskLunMapId other = (DiskLunMapId) obj;
        return Objects.equals(diskId, other.diskId)
                && Objects.equals(lunId, other.lunId);
    }

}
