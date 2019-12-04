package org.ovirt.engine.core.bll.memory;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class MemoryDisks {
    private final DiskImage memoryDisk;
    private final DiskImage metadataDisk;
    private List<DiskImage> diskList;

    public MemoryDisks(DiskImage memoryDisk, DiskImage metadataDisk) {
        this.memoryDisk = memoryDisk;
        this.metadataDisk = metadataDisk;
    }

    public DiskImage getMemoryDisk() {
        return memoryDisk;
    }

    public DiskImage getMetadataDisk() {
        return metadataDisk;
    }

    public List<DiskImage> asList() {
        if (diskList == null) {
            diskList = memoryDisk == null && metadataDisk == null ?
                    List.of() :
                    List.of(memoryDisk, metadataDisk);
        }
        return diskList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemoryDisks)) {
            return false;
        }

        MemoryDisks that = (MemoryDisks) o;
        return Objects.equals(memoryDisk, that.memoryDisk) &&
                Objects.equals(metadataDisk, that.metadataDisk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryDisk, metadataDisk);
    }
}
