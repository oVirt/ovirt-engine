package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageDynamic implements BusinessEntity<Guid>, Comparable<DiskImageDynamic> {
    private static final long serialVersionUID = 6357763045419255853L;
    private Guid id;

    private Integer readRate;

    private Integer writeRate;

    private long actualSize;

    // Latency fields are measured in second.
    private Double readLatency;

    private Double writeLatency;

    private Double flushLatency;

    public DiskImageDynamic() {
    }

    public Integer getread_rate() {
        return readRate;
    }

    public void setread_rate(Integer rate) {
        readRate = rate;
    }

    public Integer getwrite_rate() {
        return writeRate;
    }

    public void setwrite_rate(Integer rate) {
        writeRate = rate;
    }

    public Double getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(Double readLatency) {
        this.readLatency = readLatency;
    }

    public Double getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(Double writeLatency) {
        this.writeLatency = writeLatency;
    }

    public Double getFlushLatency() {
        return flushLatency;
    }

    public void setFlushLatency(Double flushLatency) {
        this.flushLatency = flushLatency;
    }

    public long getactual_size() {
        return this.actualSize;
    }

    public void setactual_size(long size) {
        this.actualSize = size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (int) (actualSize ^ (actualSize >>> 32));
        result = prime * result + ((readRate == null) ? 0 : readRate.hashCode());
        result = prime * result + ((writeRate == null) ? 0 : writeRate.hashCode());
        result = prime * result + ((readLatency == null) ? 0 : readLatency.hashCode());
        result = prime * result + ((writeLatency == null) ? 0 : writeLatency.hashCode());
        result = prime * result + ((flushLatency == null) ? 0 : flushLatency.hashCode());
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
        DiskImageDynamic other = (DiskImageDynamic) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && actualSize == other.actualSize
                && ObjectUtils.objectsEqual(readRate, other.readRate)
                && ObjectUtils.objectsEqual(writeRate, other.writeRate)
                && ObjectUtils.objectsEqual(readLatency, other.readLatency)
                && ObjectUtils.objectsEqual(writeLatency, other.writeLatency)
                && ObjectUtils.objectsEqual(flushLatency, other.flushLatency));
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public int compareTo(DiskImageDynamic o) {
        return BusinessEntityComparator.<DiskImageDynamic, Guid>newInstance().compare(this, o);
    }
}
