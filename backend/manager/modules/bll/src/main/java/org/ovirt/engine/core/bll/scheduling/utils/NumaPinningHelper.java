package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.NumaNode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class NumaPinningHelper {

    private List<Pair<VmNumaNode, Map<Integer, Collection<Integer>>>> vmNodesAndCpuPinning;
    private Map<Integer, List<Integer>> hostNodeCpuMap;

    private List<Runnable> commandStack;
    private Map<Integer, Long> hostNodeFreeMem;
    private Map<Guid, Integer> currentAssignment;

    private NumaPinningHelper(List<Pair<VmNumaNode, Map<Integer, Collection<Integer>>>> vmNodesAndCpuPinning,
            Map<Integer, List<Integer>> hostNodeCpuMap,
            Map<Integer, Long> hostNodeFreeMem) {

        this.vmNodesAndCpuPinning = vmNodesAndCpuPinning;
        this.hostNodeCpuMap = hostNodeCpuMap;
        this.hostNodeFreeMem = hostNodeFreeMem;
    }

    public static Map<Integer, List<Integer>> createCpuMap(Collection<? extends NumaNode> nodes) {
        return nodes.stream().collect(Collectors.toMap(NumaNode::getIndex, NumaNode::getCpuIds));
    }

    /**
     * Find one possible assignment of VM NUMA nodes to host NUMA nodes,
     * so that all VM nodes can fit to their assigned host nodes.
     *
     * @param vms List of VMs to check NUMA pinnig
     * @param hostNodes List of host NUMA nodes
     * @return Optional of Map from VM node index to host node index
     */
    public static Optional<Map<Guid, Integer>> findAssignment(List<VM> vms, List<VdsNumaNode> hostNodes, boolean considerCpuPinning) {

        boolean noNodes = vms.stream().allMatch(vm -> vm.getvNumaNodeList().isEmpty());
        if (noNodes) {
            return Optional.empty();
        }

        Map<Integer, Long> hostNodeFreeMem = hostNodes.stream()
                .collect(Collectors.toMap(
                        NumaNode::getIndex,
                        node -> node.getNumaNodeStatistics().getMemFree()
                ));

        // Check if all VM nodes are pinned to existing host nodes
        boolean allPinnedNodesExist = vms.stream()
                .flatMap(vm -> vm.getvNumaNodeList().stream())
                .flatMap(node -> node.getVdsNumaNodeList().stream())
                .allMatch(hostNodeFreeMem::containsKey);

        if (!allPinnedNodesExist) {
            return Optional.empty();
        }

        Map<Integer, List<Integer>> hostNodeCpuMap = considerCpuPinning ?
                hostNodes.stream()
                        .collect(Collectors.toMap(
                                NumaNode::getIndex,
                                NumaNode::getCpuIds
                        )) :
                null;

        List<Pair<VmNumaNode, Map<Integer, Collection<Integer>>>> vmNodesAndCpuPinning = vms.stream()
                .flatMap(vm -> {
                    Map<Integer, Collection<Integer>> cpuPinning = considerCpuPinning ?
                            CpuPinningHelper.parseCpuPinning(vm.getCpuPinning()).stream()
                                    .collect(Collectors.toMap(p -> p.getvCpu(), p -> p.getpCpus())) :
                            null;

                    return vm.getvNumaNodeList().stream().map(node -> new Pair<>(node, cpuPinning));
                })
                .collect(Collectors.toList());

        NumaPinningHelper helper = new NumaPinningHelper(vmNodesAndCpuPinning, hostNodeCpuMap, hostNodeFreeMem);
        return Optional.ofNullable(helper.fitNodes());
    }

    private Runnable fitNodesRunnable(int vmNumaNodeIndex) {
        return () -> {
            // Stopping condition for recursion
            if (vmNumaNodeIndex >= vmNodesAndCpuPinning.size()) {
                // If all nodes fit, clear the commandStack to skip all other commands
                // and return current assignment
                commandStack.clear();
                return;
            }

            Pair<VmNumaNode, Map<Integer, Collection<Integer>>> pair = vmNodesAndCpuPinning.get(vmNumaNodeIndex);
            VmNumaNode vmNode = pair.getFirst();
            Map<Integer, Collection<Integer>> cpuPinning = pair.getSecond();

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

                    if (cpuPinning != null && !vmNodeFitsHostNodeCpuPinning(vmNode,
                            hostNodeCpuMap.get(pinnedIndex),
                            cpuPinning)) {
                        return;
                    }

                    // The current VM node fits to the host node,
                    hostNodeFreeMem.put(pinnedIndex, hostFreeMem - vmNode.getMemTotal());
                    currentAssignment.put(vmNode.getId(), pinnedIndex);

                    // Push revert command to the stack
                    commandStack.add(() -> {
                        currentAssignment.remove(vmNode.getId());
                        hostNodeFreeMem.put(pinnedIndex, hostFreeMem);
                    });

                    // Push recursive call
                    commandStack.add(fitNodesRunnable(vmNumaNodeIndex + 1));
                });
            }
        };
    }

    private Map<Guid, Integer> fitNodes() {
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

    private boolean vmNodeFitsHostNodeCpuPinning(VmNumaNode vmNode,
            List<Integer> hostNodeCpus,
            Map<Integer, Collection<Integer>> cpuPinning) {
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
