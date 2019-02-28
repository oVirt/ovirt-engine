package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.ArrayList;
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

    private List<Runnable> commandStack;
    private Map<Integer, Long> hostNodeFreeMem;
    private Map<Integer, Integer> currentAssignment;

    private NumaPinningHelper(List<VmNumaNode> vmNodes,
            Map<Integer, Collection<Integer>> cpuPinning,
            Map<Integer, List<Integer>> hostNodeCpuMap,
            Map<Integer, Long> hostNodeFreeMem) {

        this.vmNodes = vmNodes;
        this.cpuPinning = cpuPinning;
        this.hostNodeCpuMap = hostNodeCpuMap;
        this.hostNodeFreeMem = hostNodeFreeMem;
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

        if (vmNodes.isEmpty()) {
            return Optional.empty();
        }

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

        NumaPinningHelper helper = new NumaPinningHelper(vmNodes, cpuPinning, hostNodeCpuMap, hostNodeFreeMem);
        return Optional.ofNullable(helper.fitNodes());
    }

    private Runnable fitNodesRunnable(int vmNumaNodeIndex) {
        return () -> {
            // Stopping condition for recursion
            if (vmNumaNodeIndex >= vmNodes.size()) {
                // If all nodes fit, clear the commandStack to skip all other commands
                // and return current assignment
                commandStack.clear();
                return;
            }

            VmNumaNode vmNode = vmNodes.get(vmNumaNodeIndex);

            // If the node is not pinned, skip it.
            //
            // Unpinned nodes will behave according to the default NUMA configuration on the host.
            // Here they are ignored, because we don't know if the host will use strict, interleaved or
            // preferred mode and to which host nodes they can be pinned.
            if (vmNode.getVdsNumaNodeList().isEmpty()) {
                commandStack.add(fitNodesRunnable(vmNumaNodeIndex + 1));
                return;
            }

            // The commands will be executed in reversed order, but it does not matter,
            // because this function is looking for any possible assignment
            for (Integer pinnedIndex: vmNode.getVdsNumaNodeList()) {
                commandStack.add(() -> {
                    long hostFreeMem = hostNodeFreeMem.get(pinnedIndex);
                    if (hostFreeMem < vmNode.getMemTotal()) {
                        return;
                    }

                    if (cpuPinning != null && !vmNodeFitsHostNodeCpuPinning(vmNode, hostNodeCpuMap.get(pinnedIndex))) {
                        return;
                    }

                    // The current VM node fits to the host node,
                    hostNodeFreeMem.put(pinnedIndex, hostFreeMem - vmNode.getMemTotal());
                    currentAssignment.put(vmNode.getIndex(), pinnedIndex);

                    // Push revert command to the stack
                    commandStack.add(() -> {
                        currentAssignment.remove(vmNode.getIndex());
                        hostNodeFreeMem.put(pinnedIndex, hostFreeMem);
                    });

                    // Push recursive call
                    commandStack.add(fitNodesRunnable(vmNumaNodeIndex + 1));
                });
            }
        };
    }

    private Map<Integer, Integer> fitNodes() {
        // Algorithm uses explicit stack, to avoid stack overflow
        currentAssignment = new HashMap<>();
        commandStack = new ArrayList<>();

        // Last command - assignment was not found
        commandStack.add(() -> {
            currentAssignment = null;
        });

        commandStack.add(fitNodesRunnable(0));

        while (!commandStack.isEmpty()) {
            commandStack.remove(commandStack.size() - 1).run();
        }

        return currentAssignment;
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
