package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface VmNumaNodeDao extends NumaNodeDao<VmNumaNode> {

    /**
     * Get all numa nodes of a vm by vm id
     * @param vmId
     *            the id of vm
     * @return the list of numa nodes
     */
    List<VmNumaNode> getAllVmNumaNodeByVmId(Guid vmId);

    /**
     * Get all vm numa nodes in the specified vds numa node(include real-time info)
     * @param vdsNumaNodeId
     *            the id of vds numa node
     * @return the list of numa nodes
     */
    List<VmNumaNode> getAllVmNumaNodeByVdsNumaNodeId(Guid vdsNumaNodeId);

    /**
     * Get all vm numa nodes pinned to the specified vds numa node
     * @param vdsNumaNodeId
     *            the id of vds numa node
     * @return the list of numa nodes
     */
    List<VmNumaNode> getAllPinnedVmNumaNodeByVdsNumaNodeId(Guid vdsNumaNodeId);

    /**
     * Get vm numa node information that in the vms which belong to the specified vds group
     * @param cluster
     *            the id of the cluster
     * @return a list of pairs containing the VM id and the corresponding numa node
     */
    List<Pair<Guid, VmNumaNode>> getVmNumaNodeInfoByClusterId(Guid cluster);

     /**
     * Get vm numa node information that in the vms which belong to the specified vds group
     * @param vdsGroupId
     *            the id of vds group
     *
     * @return Map with VM id as key and a list of numa nodes as value
     */
    Map<Guid, List<VmNumaNode>> getVmNumaNodeInfoByClusterIdAsMap(Guid vdsGroupId);

    /**
     * Get last pinned vds numa node index info(used for update pin info when vm migrate)
     *
     * @return a list of pairs containing the VM id and the corresponding numa node
     */
    List<Pair<Guid, Integer>> getPinnedNumaNodeIndex(Guid vmId);

    /**
     * Update vm numa node runtime pinning data of the given list of numa nodes using a more
     * efficient method to update all of them at once, rather than each at a time.
     *
     * @param vmNumaNodes
     *            the vm numa nodes to be updated
     */
    void massUpdateVmNumaNodeRuntimePinning(List<VmNumaNode> vmNumaNodes);

}
