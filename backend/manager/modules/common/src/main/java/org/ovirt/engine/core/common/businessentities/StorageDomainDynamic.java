package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -5305319985243261293L;

    public StorageDomainDynamic() {
    }

    public StorageDomainDynamic(Integer availableDiskSize, Guid id, Integer usedDiskSize) {
        this.availableDiskSize = availableDiskSize;
        this.id = id;
        this.usedDiskSize = usedDiskSize;
    }

    private Integer availableDiskSize = 0;

    public Integer getAvailableDiskSize() {
        return availableDiskSize;
    }

    public void setAvailableDiskSize(Integer availableDiskSize) {
        this.availableDiskSize = availableDiskSize;
    }

    private Guid id = new Guid();

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    private Integer usedDiskSize = 0;

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

    public int getfreeDiskInGB() {
        int availableDiskSize = getAvailableDiskSize() == null ? 0 : getAvailableDiskSize();
            return availableDiskSize;
    }

    public static StorageDomainDynamic copyOf(StorageDomainDynamic domain) {
        StorageDomainDynamic sdd = new StorageDomainDynamic();
        sdd.availableDiskSize = domain.availableDiskSize;
        sdd.id = new Guid(domain.id.getUuid());
        sdd.usedDiskSize = domain.usedDiskSize;
        return sdd;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((availableDiskSize == null) ? 0 : availableDiskSize.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((usedDiskSize == null) ? 0 : usedDiskSize.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StorageDomainDynamic other = (StorageDomainDynamic) obj;
        if (availableDiskSize == null) {
            if (other.availableDiskSize != null)
                return false;
        } else if (!availableDiskSize.equals(other.availableDiskSize))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (usedDiskSize == null) {
            if (other.usedDiskSize != null)
                return false;
        } else if (!usedDiskSize.equals(other.usedDiskSize))
            return false;
        return true;
    }
}
