package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

/**
 * Object which represents host NUMA node information
 *
 */
public class VdsNumaNode implements IVdcQueryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -683066053231559224L;

    private Guid id;

    private int index;

    private List<Integer> cpuIds;

    private long memTotal;

    private NumaNodeStatistics numaNodeStatistics;

    private Map<Integer, Integer> numaNodeDistances;

    public VdsNumaNode() {
        cpuIds = new ArrayList<Integer>();
        numaNodeDistances = new HashMap<Integer, Integer>();
    }

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

    public NumaNodeStatistics getNumaNodeStatistics() {
        return numaNodeStatistics;
    }

    public void setNumaNodeStatistics(NumaNodeStatistics numaNodeStatistics) {
        this.numaNodeStatistics = numaNodeStatistics;
    }

    /**
     * Represents the distance between this node to other nodes.
     * The key is the node index(include self), the value is the distance
     * between self and the according node.
     */
    public Map<Integer, Integer> getNumaNodeDistances() {
        return numaNodeDistances;
    }

    public void setNumaNodeDistances(Map<Integer, Integer> numaNodeDistances) {
        this.numaNodeDistances = numaNodeDistances;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cpuIds == null) ? 0 : cpuIds.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + index;
        result = prime * result + (int) (memTotal ^ (memTotal >>> 32));
        result = prime * result + ((numaNodeDistances == null) ? 0 : numaNodeDistances.hashCode());
        result = prime * result + ((numaNodeStatistics == null) ? 0 : numaNodeStatistics.hashCode());
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
        VdsNumaNode other = (VdsNumaNode) obj;
        if (cpuIds == null) {
            if (other.cpuIds != null)
                return false;
        } else if (!cpuIds.equals(other.cpuIds))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (index != other.index)
            return false;
        if (memTotal != other.memTotal)
            return false;
        if (numaNodeDistances == null) {
            if (other.numaNodeDistances != null)
                return false;
        } else if (!numaNodeDistances.equals(other.numaNodeDistances))
            return false;
        if (numaNodeStatistics == null) {
            if (other.numaNodeStatistics != null)
                return false;
        } else if (!numaNodeStatistics.equals(other.numaNodeStatistics))
            return false;
        return true;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

}
