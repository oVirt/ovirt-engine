package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public abstract class NumaNode implements Queryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -4653434518250957385L;

    private Guid id;

    private int index;

    private List<Integer> cpuIds = new ArrayList<>();

    private long memTotal;

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Integer> getCpuIds() {
        return cpuIds;
    }

    public void setCpuIds(List<Integer> cpuIds) {
        this.cpuIds = cpuIds;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(long memTotal) {
        this.memTotal = memTotal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                index,
                cpuIds,
                memTotal
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NumaNode)) {
            return false;
        }

        NumaNode other = (NumaNode) obj;
        return Objects.equals(id, other.id)
                && index == other.index
                && Objects.equals(cpuIds, other.cpuIds)
                && memTotal == other.memTotal;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }
}
