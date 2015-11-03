package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;

public interface NumaNodeDao<T extends VdsNumaNode> extends Dao{

    /**
     * Save the given list of numa nodes using a more efficient method to save all of them at
     * once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be saved
     * @param vdsId
     *            the vds id that the numa nodes belong to(leave null if save vmNumaNode)
     * @param vmId
     *            the vm id that the numa nodes belong to(leave null if save vdsNumaNode)
     */
    void massSaveNumaNode(List<T> numaNodes, Guid vdsId, Guid vmId);

    /**
     * Update the statistics data of the given list of numa nodes using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be updated
     */
    void massUpdateNumaNodeStatistics(List<T> numaNodes);

    /**
     * Update non-statistics data of the given list of numa nodes using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be updated
     */
    void massUpdateNumaNode(List<T> numaNodes);

    /**
     * Remove numa nodes using a more efficient method to remove all of them at once,
     * rather than each at a time.
     *
     * @param numaNodeIds
     *            the numa node ids to be removed
     */
    void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds);
}
