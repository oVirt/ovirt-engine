package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Object which represents host NUMA node information
 *
 */
public class VdsNumaNode extends NumaNode {

    private static final long serialVersionUID = -683066053231559224L;

    private NumaNodeStatistics numaNodeStatistics;

    private Map<Integer, Integer> numaNodeDistances = new HashMap<>();

    public VdsNumaNode() {
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
                super.hashCode(),
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
        return super.equals(obj)
                && Objects.equals(numaNodeDistances, other.numaNodeDistances)
                && Objects.equals(numaNodeStatistics, other.numaNodeStatistics);
    }
}
