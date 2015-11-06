package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class VmBlockJob extends VmJob {

    private static final long serialVersionUID = -7009231492231684166L;

    private VmBlockJobType blockJobType;
    private Long bandwidth;
    private Long cursorCur;
    private Long cursorEnd;
    private Guid imageGroupId;

    public VmBlockJob() {
        setJobType(VmJobType.BLOCK);
        blockJobType = VmBlockJobType.UNKNOWN;
        bandwidth = 0L;
        cursorCur = 0L;
        cursorEnd = 0L;
        imageGroupId = Guid.Empty;
    }

    public VmBlockJobType getBlockJobType() {
        return blockJobType;
    }

    public void setBlockJobType(VmBlockJobType blockJobType) {
        this.blockJobType = blockJobType;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Long getCursorCur() {
        return cursorCur;
    }

    public void setCursorCur(Long cursorCur) {
        this.cursorCur = cursorCur;
    }

    public Long getCursorEnd() {
        return cursorEnd;
    }

    public void setCursorEnd(Long cursorEnd) {
        this.cursorEnd = cursorEnd;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
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
        VmBlockJob other = (VmBlockJob) obj;
        return (super.equals(other)
                && Objects.equals(bandwidth, other.bandwidth)
                && blockJobType == other.blockJobType
                && Objects.equals(cursorCur, other.cursorCur)
                && Objects.equals(cursorEnd, other.cursorEnd)
                && Objects.equals(imageGroupId, other.imageGroupId));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (blockJobType == null ? 0 : blockJobType.hashCode());
        result = prime * result + (bandwidth == null ? 0 : bandwidth.hashCode());
        result = prime * result + (cursorCur == null ? 0 : cursorCur.hashCode());
        result = prime * result + (cursorEnd == null ? 0 : cursorEnd.hashCode());
        result = prime * result + (imageGroupId == null ? 0 : imageGroupId.hashCode());
        return result;
    }
}
