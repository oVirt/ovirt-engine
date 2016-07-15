package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public interface VmNumaNodeDao extends Dao {

    /**
     * Get all numa nodes of a vm by vm id
     * @param vmId
     *            the id of vm
     * @return the list of numa nodes
     */
    List<VmNumaNode> getAllVmNumaNodeByVmId(Guid vmId);

     /**
     * Get vm numa node information that in the vms which belong to the specified vds group
     * @param clusterId
     *            the id of the cluster
     *
     * @return Map with VM id as key and a list of numa nodes as value
     */
    Map<Guid, List<VmNumaNode>> getVmNumaNodeInfoByClusterId(Guid clusterId);

    /**
     * Save the given list of numa nodes using a more efficient method to save all of them at
     * once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be saved
     * @param vmId
     *            the vm id that the numa nodes belong to(leave null if save vdsNumaNode)
     */
    void massSaveNumaNode(List<VmNumaNode> numaNodes, Guid vmId);

    /**
     * Update non-statistics data of the given list of numa nodes using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param numaNodes
     *            the numa nodes to be updated
     */
    void massUpdateNumaNode(List<VmNumaNode> numaNodes);

    /**
     * Remove numa nodes using a more efficient method to remove all of them at once,
     * rather than each at a time.
     *
     * @param numaNodeIds
     *            the numa node ids to be removed
     */
    void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds);
}
