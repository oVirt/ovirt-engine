package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainOvfInfo implements BusinessEntity<Guid> {
    private Guid storageDomainId;
    private List<Guid> storedOvfIds;
    private Guid ovfDiskId;
    private StorageDomainOvfInfoStatus status;
    private Date lastUpdated;

    public StorageDomainOvfInfo(Guid storageDomainId, List<Guid> storedOvfIds,
            Guid ovfDiskId, StorageDomainOvfInfoStatus status, Date lastUpdated) {
        this.storageDomainId = storageDomainId;
        this.storedOvfIds = storedOvfIds;
        this.ovfDiskId = ovfDiskId;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public StorageDomainOvfInfo() {
    }

    public StorageDomainOvfInfoStatus getStatus() {
        return status;
    }

    public void setStatus(StorageDomainOvfInfoStatus status) {
        this.status = status;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getOvfDiskId() {
        return ovfDiskId;
    }

    public void setOvfDiskId(Guid ovfDiskId) {
        this.ovfDiskId = ovfDiskId;
    }

    @Override
    public Guid getId() {
        return getOvfDiskId();
    }

    @Override
    public void setId(Guid id) {
        setOvfDiskId(id);
    }

    public List<Guid> getStoredOvfIds() {
        return storedOvfIds;
    }

    public void setStoredOvfIds(List<Guid> storedOvfIds) {
        this.storedOvfIds = storedOvfIds;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageDomainOvfInfo)) {
            return false;
        }

        StorageDomainOvfInfo ovfInfo = (StorageDomainOvfInfo) o;
        return status == ovfInfo.status
                && Objects.equals(ovfDiskId, ovfInfo.ovfDiskId)
                && Objects.equals(storageDomainId, ovfInfo.storageDomainId)
                && Objects.equals(lastUpdated, ovfInfo.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                storageDomainId,
                ovfDiskId,
                status,
                lastUpdated
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("storageDomainId", storageDomainId)
                .append("storedOvfIds", storedOvfIds)
                .append("ovfDiskId", ovfDiskId)
                .append("status", status)
                .append("lastUpdated", lastUpdated)
                .build();
    }
}
