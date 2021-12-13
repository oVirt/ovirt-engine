package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HugePage implements Serializable {
    private static final long serialVersionUID = -7900304010567972606L;
    private Integer sizeKB;
    private Integer free;

    public HugePage() {
    }

    public HugePage(Integer sizeKB, Integer free) {
        this.sizeKB = sizeKB;
        this.free = free;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HugePage)) {
            return false;
        }
        HugePage other = (HugePage) obj;
        return Objects.equals(sizeKB, other.sizeKB)
                && Objects.equals(free, other.free);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sizeKB,
                free
        );
    }

    public Integer getSizeKB() {
        return sizeKB;
    }

    public void setSizeKB(Integer sizeKB) {
        this.sizeKB = sizeKB;
    }

    public Integer getFree() {
        return free;
    }

    public void setFree(Integer free) {
        this.free = free;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("sizeKB", getSizeKB())
                .append("free", getFree())
                .build();
    }
}
