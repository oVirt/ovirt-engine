package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        cpuIds = new ArrayList<>();
        numaNodeDistances = new HashMap<>();
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
        return Objects.hash(
                cpuIds,
                id,
                index,
                memTotal,
                numaNodeDistances,
                numaNodeStatistics
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsNumaNode)) {
            return false;
        }
        VdsNumaNode other = (VdsNumaNode) obj;
        return Objects.equals(cpuIds, other.cpuIds)
                && Objects.equals(id, other.id)
                && index == other.index
                && memTotal == other.memTotal
                && Objects.equals(numaNodeDistances, other.numaNodeDistances)
                && Objects.equals(numaNodeStatistics, numaNodeStatistics);
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

}
