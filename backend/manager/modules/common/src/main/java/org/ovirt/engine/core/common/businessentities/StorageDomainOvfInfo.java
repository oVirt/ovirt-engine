package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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
        if (this == o) return true;
        if (!(o instanceof StorageDomainOvfInfo)) return false;

        StorageDomainOvfInfo ovfInfo = (StorageDomainOvfInfo) o;

        if (status != ovfInfo.status) return false;
        if (!ObjectUtils.objectsEqual(ovfDiskId, ovfInfo.ovfDiskId)) return false;
        if (!ObjectUtils.objectsEqual(storageDomainId, ovfInfo.storageDomainId)) return false;
        if (!ObjectUtils.objectsEqual(lastUpdated, ovfInfo.lastUpdated)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = storageDomainId != null ? storageDomainId.hashCode() : 0;
        result = 31 * result + (ovfDiskId != null ? ovfDiskId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        return result;
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
