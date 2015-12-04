package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
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

    public Integer getReadRate() {
        return readRate;
    }

    public void setReadRate(Integer rate) {
        readRate = rate;
    }

    public Integer getWriteRate() {
        return writeRate;
    }

    public void setWriteRate(Integer rate) {
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

    public long getActualSize() {
        return this.actualSize;
    }

    public void setActualSize(long size) {
        this.actualSize = size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                actualSize,
                readRate,
                writeRate,
                readLatency,
                writeLatency,
                flushLatency
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiskImageDynamic)) {
            return false;
        }
        DiskImageDynamic other = (DiskImageDynamic) obj;
        return Objects.equals(id, other.id)
                && actualSize == other.actualSize
                && Objects.equals(readRate, other.readRate)
                && Objects.equals(writeRate, other.writeRate)
                && Objects.equals(readLatency, other.readLatency)
                && Objects.equals(writeLatency, other.writeLatency)
                && Objects.equals(flushLatency, other.flushLatency);
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
