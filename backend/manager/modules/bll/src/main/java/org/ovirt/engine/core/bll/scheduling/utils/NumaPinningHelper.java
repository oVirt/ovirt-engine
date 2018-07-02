package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.NumaNode;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;


public class NumaPinningHelper {

    /**
     * Find one possible assignment of VM NUMA nodes to host NUMA nodes,
     * so that all VM nodes can fit to their assigned host nodes.
     *
     * @param vmNodes List of VM NUMA nodes
     * @param hostNodes List of host NUMA nodes
     * @return Optional of Map from VM node index to host node index
     */
    public static Optional<Map<Integer, Integer>> findAssignment(List<VmNumaNode> vmNodes, List<VdsNumaNode> hostNodes) {
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

        return fitNodes(vmNodes, 0, hostNodeFreeMem);
    }

    private static Optional<Map<Integer, Integer>> fitNodes(List<VmNumaNode> vmNodes,
            int vmNumaNodeIndex,
            Map<Integer, Long> hostNodeFreeMem) {

        // Stopping condition for recursion
        // If all nodes fit, return an empty map
        if (vmNumaNodeIndex >= vmNodes.size()) {
            return Optional.of(new HashMap<>(vmNodes.size()));
        }

        VmNumaNode vmNode = vmNodes.get(vmNumaNodeIndex);
        for (Integer pinnedIndex: vmNode.getVdsNumaNodeList()) {
            long hostFreeMem = hostNodeFreeMem.get(pinnedIndex);
            if (hostFreeMem < vmNode.getMemTotal()) {
                continue;
            }

            // The current VM node fits to the host node,
            // store new free memory value in the list
            hostNodeFreeMem.put(pinnedIndex, hostFreeMem - vmNode.getMemTotal());

            // Recursive call to check if the rest of the nodes fit
            Optional<Map<Integer, Integer>> othersFit = fitNodes(vmNodes, vmNumaNodeIndex + 1, hostNodeFreeMem);

            // if a possible assignment was found, store it in the output map and return
            if (othersFit.isPresent()) {
                othersFit.get().put(vmNode.getIndex(), pinnedIndex);
                return othersFit;
            }

            // The rest of the VM nodes do not fit, return the old value to the list
            hostNodeFreeMem.put(pinnedIndex, hostFreeMem);
        }

        return Optional.empty();
    }

}
