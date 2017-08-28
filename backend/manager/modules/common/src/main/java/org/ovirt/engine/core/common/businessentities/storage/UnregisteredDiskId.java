package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class UnregisteredDiskId implements Serializable, Comparable<UnregisteredDiskId> {

    private static final long serialVersionUID = 2012590627292239007L;
    private Guid diskId;
    private Guid storageDomainId;

    public UnregisteredDiskId() {
    }

    public UnregisteredDiskId(Guid diskId, Guid storageDomainId) {
        this.diskId = diskId;
        this.storageDomainId = storageDomainId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnregisteredDiskId that = (UnregisteredDiskId) o;
        return Objects.equals(diskId, that.diskId) &&
                Objects.equals(storageDomainId, that.storageDomainId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diskId, storageDomainId);
    }

    @Override
    public int compareTo(UnregisteredDiskId o) {
        int idComparison = diskId.compareTo(o.diskId);
        if (idComparison == 0) {
            return storageDomainId.compareTo(o.storageDomainId);
        }

        return idComparison;
    }
}
