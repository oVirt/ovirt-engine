package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSizeInfo implements Serializable {
    private static final long serialVersionUID = -5145858224564431005L;

    private Guid volumeId;

    private Long totalSize;

    private Long freeSize;

    private Long usedSize;

    public GlusterVolumeSizeInfo() {
    }

    public GlusterVolumeSizeInfo(Long totalSize, Long freeSize, Long usedSize) {
        this.totalSize = totalSize;
        this.freeSize = freeSize;
        this.usedSize = usedSize;
    }

    public Guid getVolumeId() {
        return this.volumeId;
    }

    public void setVolumeId(Guid id) {
        this.volumeId = id;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(Long freeSize) {
        this.freeSize = freeSize;
    }

    public Long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(Long usedSize) {
        this.usedSize = usedSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((volumeId == null) ? 0 : volumeId.hashCode());
        result = prime * result + ((totalSize == null) ? 0 : totalSize.hashCode());
        result = prime * result + ((freeSize == null) ? 0 : freeSize.hashCode());
        result = prime * result + ((usedSize == null) ? 0 : usedSize.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterVolumeSizeInfo)) {
            return false;
        }
        GlusterVolumeSizeInfo sizeInfo = (GlusterVolumeSizeInfo) obj;

        if (!Objects.equals(volumeId, sizeInfo.getVolumeId())) {
            return false;
        }

        if (!Objects.equals(totalSize, sizeInfo.getTotalSize())) {
            return false;
        }

        if (!Objects.equals(freeSize, sizeInfo.getFreeSize())) {
            return false;
        }

        if (!Objects.equals(usedSize, sizeInfo.getUsedSize())) {
            return false;
        }

        return true;
    }
}
