package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSizeInfo implements Serializable {
    private static final long serialVersionUID = -5145858224564431005L;

    private Guid volumeId;

    private Long totalSize;

    private Long freeSize;

    private Long confirmedFreeSize;

    private Integer vdoSavings;

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

    public Long getConfirmedFreeSize() {
        return confirmedFreeSize;
    }

    public void setConfirmedFreeSize(Long confirmedFreeSize) {
        this.confirmedFreeSize = confirmedFreeSize;
    }

    public Integer getVdoSavings() {
        return vdoSavings;
    }

    public void setVdoSavings(Integer vdoSavings) {
        this.vdoSavings = vdoSavings;
    }

    public Long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(Long usedSize) {
        this.usedSize = usedSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                volumeId,
                totalSize,
                freeSize,
                confirmedFreeSize,
                vdoSavings,
                usedSize
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeSizeInfo)) {
            return false;
        }
        GlusterVolumeSizeInfo other = (GlusterVolumeSizeInfo) obj;
        return Objects.equals(volumeId, other.volumeId)
                && Objects.equals(totalSize, other.totalSize)
                && Objects.equals(freeSize, other.freeSize)
                && Objects.equals(usedSize, other.usedSize)
                && Objects.equals(confirmedFreeSize, other.confirmedFreeSize)
                && Objects.equals(vdoSavings, other.vdoSavings);
    }
}
