package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -5305319985243261293L;

    public StorageDomainDynamic() {
        this (null, Guid.Empty, null);
    }

    public StorageDomainDynamic(Integer availableDiskSize, Guid id, Integer usedDiskSize) {
        this.availableDiskSize = availableDiskSize;
        this.id = id;
        this.usedDiskSize = usedDiskSize;
        this.externalStatus = ExternalStatus.Ok;
    }

    private Integer availableDiskSize;
    private  ExternalStatus externalStatus;
    private boolean containsUnregisteredEntities;

    public boolean isContainsUnregisteredEntities() {
        return containsUnregisteredEntities;
    }

    public void setContainsUnregisteredEntities(boolean containsUnregisteredEntities) {
        this.containsUnregisteredEntities = containsUnregisteredEntities;
    }

    public Integer getAvailableDiskSize() {
        return availableDiskSize;
    }

    public void setAvailableDiskSize(Integer availableDiskSize) {
        this.availableDiskSize = availableDiskSize;
    }

    private Guid id;

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    private Integer usedDiskSize;

    public Integer getUsedDiskSize() {
        return usedDiskSize;
    }

    public void setUsedDiskSize(Integer usedDiskSize) {
        this.usedDiskSize = usedDiskSize;
    }

    public double getfreeDiskPercent() {
        int usedDiskSize = getUsedDiskSize() == null ? 0 : getUsedDiskSize();
        int availableDiskSize = getAvailableDiskSize() == null ? 0 : getAvailableDiskSize();
        double totalSize = usedDiskSize + availableDiskSize;
        return totalSize != 0 ? (availableDiskSize / totalSize) * 100 : 0.0;
    }

    public ExternalStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(ExternalStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                availableDiskSize,
                usedDiskSize,
                externalStatus,
                containsUnregisteredEntities
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageDomainDynamic)) {
            return false;
        }
        StorageDomainDynamic other = (StorageDomainDynamic) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(availableDiskSize, other.availableDiskSize)
                && Objects.equals(usedDiskSize, other.usedDiskSize)
                && Objects.equals(externalStatus, other.externalStatus)
                && containsUnregisteredEntities == other.containsUnregisteredEntities;
    }
}
