package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;

public interface VdsNumaNodeDao extends Dao {

    /**
     * Get all numa nodes of a vds by vds id
     * @param vdsId
     *            the id of vds
     * @return the list of numa nodes
     */
    List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId);

    /**
     * Save the given list of numa nodes using a more efficient method to save all of them at
     * once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be saved
     * @param vdsId
     *            the vds id that the numa nodes belong to(leave null if save vmNumaNode)
     */
    void massSaveNumaNode(List<VdsNumaNode> numaNodes, Guid vdsId);

    /**
     * Update non-statistics data of the given list of numa nodes using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be updated
     */
    void massUpdateNumaNode(List<VdsNumaNode> numaNodes);

    /**
     * Remove numa nodes using a more efficient method to remove all of them at once,
     * rather than each at a time.
     *
     * @param numaNodeIds
     *            the numa node ids to be removed
     */
    void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds);

    /**
     * Update the statistics data of the given list of numa nodes using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be updated
     */
    void massUpdateNumaNodeStatistics(List<VdsNumaNode> numaNodes);
}
