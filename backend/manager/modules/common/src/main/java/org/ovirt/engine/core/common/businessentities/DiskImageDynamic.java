package org.ovirt.engine.core.common.businessentities;

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
        result = prime * result + (int) (actualSize ^ (actualSize >>> 32));
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((readRate == null) ? 0 : readRate.hashCode());
        result = prime * result + ((writeRate == null) ? 0 : writeRate.hashCode());
        result = prime * result + ((writeLatency == null) ? 0 : writeLatency.hashCode());
        result = prime * result + ((readLatency == null) ? 0 : readLatency.hashCode());
        result = prime * result + ((flushLatency == null) ? 0 : flushLatency.hashCode());
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
        DiskImageDynamic other = (DiskImageDynamic) obj;
        if (actualSize != other.actualSize)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (readRate == null) {
            if (other.readRate != null)
                return false;
        } else if (!readRate.equals(other.readRate))
            return false;
        if (writeRate == null) {
            if (other.writeRate != null)
                return false;
        } else if (!writeRate.equals(other.writeRate))
            return false;
        if (readLatency == null) {
            if (other.readLatency != null)
                return false;
        } else if (!readLatency.equals(other.readLatency))
            return false;
        if (writeLatency == null) {
            if (other.writeLatency != null)
                return false;
        } else if (!writeLatency.equals(other.writeLatency))
            return false;
        if (flushLatency == null) {
            if (other.flushLatency != null)
                return false;
        } else if (!flushLatency.equals(other.flushLatency))
            return false;
        return true;
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
        return BusinessEntityGuidComparator.<DiskImageDynamic>newInstance().compare(this,o);
    }
}
