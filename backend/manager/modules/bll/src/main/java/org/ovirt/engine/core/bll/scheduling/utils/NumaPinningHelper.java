package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.NumaNode;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;

public class NumaPinningHelper {

    private List<VmNumaNode> vmNodes;
    private Map<Integer, Collection<Integer>> cpuPinning;
    private Map<Integer, List<Integer>> hostNodeCpuMap;

    private NumaPinningHelper(List<VmNumaNode> vmNodes,
            Map<Integer, Collection<Integer>> cpuPinning,
            Map<Integer, List<Integer>> hostNodeCpuMap) {

        this.vmNodes = vmNodes;
        this.cpuPinning = cpuPinning;
        this.hostNodeCpuMap = hostNodeCpuMap;
    }

    public static Optional<Map<Integer, Integer>> findAssignment(List<VmNumaNode> vmNodes, List<VdsNumaNode> hostNodes) {
        return findAssignment(vmNodes, hostNodes, null);
    }

    /**
     * Find one possible assignment of VM NUMA nodes to host NUMA nodes,
     * so that all VM nodes can fit to their assigned host nodes.
     *
     * @param vmNodes List of VM NUMA nodes
     * @param hostNodes List of host NUMA nodes
     * @param cpuPinning Map of host CPU index to collection of VM cpu indices
     * @return Optional of Map from VM node index to host node index
     */
    public static Optional<Map<Integer, Integer>> findAssignment(List<VmNumaNode> vmNodes,
            List<VdsNumaNode> hostNodes,
            Map<Integer, Collection<Integer>> cpuPinning) {

        Map<Integer, Long> hostNodeFreeMem = hostNodes.stream()
                .collect(Collectors.toMap(
                        NumaNode::getIndex,
                        node -> node.getNumaNodeStatistics().getMemFree()
                ));

        // Check if all VM nodes are pinned to existing host nodes
        boolean allPinnedNodesExist = vmNodes.stream()
                .flatMap(node -> node.getVdsNumaNodeList().stream())
                .allMatch(hostNodeFreeMem::containsKey);

        if (!allPinnedNodesExist) {
            return Optional.empty();
        }

        if (cpuPinning != null && cpuPinning.isEmpty()) {
            cpuPinning = null;
        }

        Map<Integer, List<Integer>> hostNodeCpuMap = cpuPinning == null ?
                null :
                hostNodes.stream()
                        .collect(Collectors.toMap(
                            NumaNode::getIndex,
                            NumaNode::getCpuIds
                        ));

        NumaPinningHelper helper = new NumaPinningHelper(vmNodes, cpuPinning, hostNodeCpuMap);
        Map<Integer, Integer> assignment = helper.fitNodes(0, hostNodeFreeMem);
        return Optional.ofNullable(assignment);
    }

    private Map<Integer, Integer> fitNodes(int vmNumaNodeIndex, Map<Integer, Long> hostNodeFreeMem) {
        // Stopping condition for recursion
        // If all nodes fit, return an empty map
        if (vmNumaNodeIndex >= vmNodes.size()) {
            return new HashMap<>(vmNodes.size());
        }

        VmNumaNode vmNode = vmNodes.get(vmNumaNodeIndex);

        // If the node is not pinned, skip it.
        //
        // Unpinned nodes will behave according to the default NUMA configuration on the host.
        // Here they are ignored, because we don't know if the host will use strict, interleaved or
        // preferred mode and to which host nodes they can be pinned.
        if (vmNode.getVdsNumaNodeList().isEmpty()) {
            return fitNodes(vmNumaNodeIndex + 1, hostNodeFreeMem);
        }

        for (Integer pinnedIndex: vmNode.getVdsNumaNodeList()) {
            long hostFreeMem = hostNodeFreeMem.get(pinnedIndex);
            if (hostFreeMem < vmNode.getMemTotal()) {
                continue;
            }

            if (cpuPinning != null) {
                if (!vmNodeFitsHostNodeCpuPinning(vmNode, hostNodeCpuMap.get(pinnedIndex))) {
                    continue;
                }
            }
            // The current VM node fits to the host node,
            // store new free memory value in the list
            hostNodeFreeMem.put(pinnedIndex, hostFreeMem - vmNode.getMemTotal());

            // Recursive call to check if the rest of the nodes fit
            Map<Integer, Integer> othersFit = fitNodes(vmNumaNodeIndex + 1, hostNodeFreeMem);

            // if a possible assignment was found, store it in the output map and return
            if (othersFit != null) {
                othersFit.put(vmNode.getIndex(), pinnedIndex);
                return othersFit;
            }

            // The rest of the VM nodes do not fit, return the old value to the list
            hostNodeFreeMem.put(pinnedIndex, hostFreeMem);
        }

        return null;
    }

    private boolean vmNodeFitsHostNodeCpuPinning(VmNumaNode vmNode, List<Integer> hostNodeCpus) {
        for (Integer vmCpuId: vmNode.getCpuIds()) {
            Collection<Integer> pinnedCpus = cpuPinning.get(vmCpuId);
            if (pinnedCpus == null) {
                continue;
            }

            if (pinnedCpus.stream().anyMatch(hostNodeCpus::contains)) {
                continue;
            }

            return false;
        }

        return true;
    }
}
