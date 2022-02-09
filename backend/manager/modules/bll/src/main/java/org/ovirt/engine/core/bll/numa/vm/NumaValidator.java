package org.ovirt.engine.core.bll.numa.vm;

import static java.lang.Integer.min;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;

@Singleton
public class NumaValidator {

    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final VdsDao vdsDao;

    @Inject
    NumaValidator(VdsNumaNodeDao vdsNumaNodeDao, VdsDao vdsDao) {
        this.vdsNumaNodeDao = Objects.requireNonNull(vdsNumaNodeDao);
        this.vdsDao = Objects.requireNonNull(vdsDao);
    }

    /**
     * preferred supports single pinned vNUMA node (without that VM fails to run in libvirt)
     */
    private ValidationResult checkNumaPreferredTuneMode(List<VmNumaNode> vmNumaNodes) {
        // check tune mode
        if (vmNumaNodes.stream().map(VmNumaNode::getNumaTuneMode)
                .allMatch(tune -> tune != NumaTuneMode.PREFERRED)) {
            return ValidationResult.VALID;
        }

        // check single node pinned
        if (vmNumaNodes.size() == 1) {
            List<Integer> vdsNumaNodeList = vmNumaNodes.get(0).getVdsNumaNodeList();
            boolean pinnedToSingleNode = vdsNumaNodeList != null
                    && vdsNumaNodeList.size() == 1;
            if (pinnedToSingleNode) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    /**
     * Check if we have enough virtual cpus for the virtual NUMA nodes
     *
     * @param numaNodeCount number of virtual NUMA nodes
     * @param cpuCores      number of virtual cpu cores
     * @return the validation result
     */
    private ValidationResult checkVmNumaNodeCount(int numaNodeCount, int cpuCores) {

        if (cpuCores < numaNodeCount) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS,
                    String.format("$numaNodes %d", numaNodeCount),
                    String.format("$cpus %d", cpuCores));
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if every CPU is assigned to at most one virtual NUMA node
     *
     * @param cpuCores    number of virtual cpu cores
     * @param vmNumaNodes list of virtual NUMA nodes
     * @return the validation result
     */
    private ValidationResult checkVmNumaCpuAssignment(int cpuCores, List<VmNumaNode> vmNumaNodes) {
        List<Integer> cpuIds = vmNumaNodes.stream()
                .flatMap(node -> node.getCpuIds().stream())
                .collect(Collectors.toList());

        if (cpuIds.isEmpty()) {
            return ValidationResult.VALID;
        }

        int minId = Collections.min(cpuIds);
        int maxId = Collections.max(cpuIds);

        if (minId < 0 || maxId >= cpuCores) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_INVALID_CPU_ID,
                    String.format("$cpuIndex %d", (minId < 0) ? minId : maxId),
                    String.format("$cpuIndexMax %d", cpuCores - 1));
        }

        List<Integer> duplicateIds = cpuIds.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()))
                .entrySet().stream()
                .filter(a -> a.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicateIds.isEmpty()) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_DUPLICATE_CPU_IDS,
                    String.format("$cpuIndexes %s", duplicateIds.stream()
                            .map(i -> i.toString())
                            .collect(Collectors.joining(", "))));
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if the total memory of NUMA nodes is less or equal to the total VM memory
     *
     * @param totalVmMemory to check
     * @param vmNumaNodes list of virtual NUMA nodes
     * @return the validation result
     */
    private ValidationResult checkVmNumaTotalMemory(long totalVmMemory, List<VmNumaNode> vmNumaNodes) {
        long totalNumaNodeMem = vmNumaNodes.stream()
                .mapToLong(VmNumaNode::getMemTotal)
                .sum();

        if (totalNumaNodeMem > totalVmMemory) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_TOTAL_MEMORY_ERROR,
                    String.format("$vmNodeTotalMemory %d", totalNumaNodeMem),
                    String.format("$vmTotalMemory %d", totalVmMemory));
        }

        return ValidationResult.VALID;
    }

    private ValidationResult checkHugepagesFitNumaNodes(final VmBase vmBase, final List<VmNumaNode> vmNumaNodes) {
        Optional<Integer> hugePageSizeKB = HugePageUtils.getHugePageSize(vmBase);
        if (!hugePageSizeKB.isPresent()) {
            return ValidationResult.VALID;
        }

        // NUMA node size must be a multiple of hugepage size
        if (vmNumaNodes.stream().allMatch(node -> (node.getMemTotal() * 1024) % hugePageSizeKB.get() == 0)) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.VM_NUMA_NODE_NOT_MULTIPLE_OF_HUGEPAGE);
    }

    /**
     * Check if the provided NUMA nodes do not containe the same NUMA node index more than once
     *
     * @param vmNumaNodes to check for duplicates
     * @return {@link ValidationResult#VALID} if no duplicates exist
     */
    public ValidationResult checkVmNumaIndexDuplicates(final List<VmNumaNode> vmNumaNodes) {
        Set<Integer> indices = new HashSet<>();
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            if (!indices.add(vmNumaNode.getIndex())) {
                return new ValidationResult(EngineMessage.VM_NUMA_NODE_INDEX_DUPLICATE,
                        String.format("$nodeIndex %d", vmNumaNode.getIndex()));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if the indices of the provided NUMA nodes are continuous
     *
     * @param vmNumaNodes to check if indices are continuous
     * @return {@link ValidationResult#VALID} if no indices are missing
     */
    public ValidationResult checkVmNumaIndexContinuity(final List<VmNumaNode> vmNumaNodes) {
        Set<Integer> indices = vmNumaNodes.stream().map(VmNumaNode::getIndex).collect(Collectors.toSet());
        List<Integer> missingIndices = IntStream.range(0, vmNumaNodes.size()).filter(i -> !indices.contains(i))
                .boxed().collect(Collectors.toList());

        if (!missingIndices.isEmpty()) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_NON_CONTINUOUS_INDEX,
                    String.format("$nodeCount %d", vmNumaNodes.size()),
                    String.format("$minIndex %d", 0),
                    String.format("$maxIndex %d", indices.size() - 1),
                    String.format("$missingIndices %s", formatMissingIndices(missingIndices)));
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if the NUMA configuration on the VM is consistent. This only checks the VM and the host pinning. No
     * compatibility checks regarding host(s) are performed
     *
     * @param vm to check
     * @return validation result
     */
    public ValidationResult validateVmNumaConfig(final VM vm, final List<VmNumaNode> vmNumaNodes) {

        if (vmNumaNodes.isEmpty()) {
            return ValidationResult.VALID;
        }

        ValidationResult validationResult = checkNumaPreferredTuneMode(vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaNodeCount(vmNumaNodes.size(), VmCpuCountHelper.getDynamicNumOfCpu(vm));
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaIndexDuplicates(vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaIndexContinuity(vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaCpuAssignment(VmCpuCountHelper.getDynamicNumOfCpu(vm), vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaTotalMemory(vm.getVmMemSizeMb(), vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkHugepagesFitNumaNodes(vm.getStaticData(), vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if a VM can run on pinned host(s) with the provided NUMA configuration. The NUMA nodes for
     * validation need to be passed in separately because the NUMA nodes are not necessarily part of the VM when the
     * validation takes place.
     *
     * @param vm            with NUMA nodes
     * @param vmNumaNodes   to use for validation
     * @param hostsNumaNodesMap list of NUMA nodes per each pinned host
     * @return whether the vm can run on all pinned hosts (hostsNodesMap) or not
     */
    public ValidationResult validateNumaCompatibility(final VM vm, final List<VmNumaNode> vmNumaNodes,
    final Map<Guid, List<VdsNumaNode>> hostsNumaNodesMap) {
        for (Map.Entry<Guid, List<VdsNumaNode>> entry : hostsNumaNodesMap.entrySet()) {
            Guid pinnedVds = entry.getKey();
            List<VdsNumaNode> pinnedVdsNumaNodes = entry.getValue();

            if (pinnedVdsNumaNodes == null || pinnedVdsNumaNodes.isEmpty()) {
                return new ValidationResult(EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY,
                        String.format("$hostName %s", getHostNameOrId(pinnedVds)));
            }
            // One node is equal to no NUMA node architecture present
            if (pinnedVdsNumaNodes.size() == 1) {
                return new ValidationResult(EngineMessage.HOST_NUMA_NOT_SUPPORTED,
                        String.format("$hostName %s", getHostNameOrId(pinnedVds)));
            }

            final Map<Integer, VdsNumaNode> hostNodeIndexToNodeMap = new HashMap<>();
            pinnedVdsNumaNodes.forEach(node -> hostNodeIndexToNodeMap.put(node.getIndex(), node));

            for (VmNumaNode vmNumaNode : vmNumaNodes) {
                for (Integer vdsPinnedIndex : vmNumaNode.getVdsNumaNodeList()) {
                    if (vdsPinnedIndex == null) {
                        return new ValidationResult(EngineMessage.VM_NUMA_NODE_PINNED_INDEX_ERROR);
                    }

                    if (!hostNodeIndexToNodeMap.containsKey(vdsPinnedIndex)) {
                        return new ValidationResult(EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX,
                                String.format("$vdsNodeIndex %d", vdsPinnedIndex),
                                String.format("$hostName %s", getHostNameOrId(pinnedVds)));
                    }

                    if (vmNumaNode.getNumaTuneMode() == NumaTuneMode.STRICT
                            && vmNumaNode.getMemTotal() > hostNodeIndexToNodeMap.get(vdsPinnedIndex).getMemTotal()) {
                        return new ValidationResult(EngineMessage.VM_NUMA_NODE_MEMORY_ERROR,
                                String.format("$vmNodeIndex %d", vmNumaNode.getIndex()),
                                String.format("$hostName %s", getHostNameOrId(pinnedVds)),
                                String.format("$hostNodeIndex %d", vdsPinnedIndex));
                    }
                }
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Check the whole NUMA configuration of a VM. The NUMA nodes for validation need to be passed in separately because
     * the NUMA nodes are not necessarily part of the VM when the validation takes place.
     *
     * @param vm          to check comaptiblity with
     * @param vmNumaNodes to use for validation
     * @return the validation result
     */
    public ValidationResult checkVmNumaNodesIntegrity(final VM vm, final List<VmNumaNode> vmNumaNodes) {
        if (vmNumaNodes.isEmpty()) {
            return ValidationResult.VALID;
        }

        // Check VM's NUMA configuration
        ValidationResult validationResult = validateVmNumaConfig(vm, vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        // Check if the VM is pinned to at least one host
        if (vm.getDedicatedVmForVdsList().isEmpty() && !VmCpuCountHelper.isAutoPinning(vm)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_AT_LEAST_ONE_HOST);
        }

        // check that VM's NUMA policy fits all pinned/assigned hosts NUMA policy
        final Map<Guid, List<VdsNumaNode>> hostsNumaNodesMap = new HashMap<>();
        if (VmCpuCountHelper.isAutoPinning(vm)) {
            return ValidationResult.VALID;
        } else {
            vm.getDedicatedVmForVdsList()
                    .forEach(vdsId -> hostsNumaNodesMap.put(vdsId, vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vdsId)));
        }

        return validateNumaCompatibility(vm, vmNumaNodes, hostsNumaNodesMap);
    }

    private String formatMissingIndices(List<Integer> missingIndices) {
        String str = StringUtils.join(missingIndices.subList(0, min(10, missingIndices.size())), ", ");
        if (missingIndices.size() > 10) {
            str = str + ", ...";
        }
        return str;
    }

    private String getHostNameOrId(Guid vdsId) {
        VDS host = vdsDao.get(vdsId);

        return host == null ? vdsId.toString() : host.getName();
    }
}
