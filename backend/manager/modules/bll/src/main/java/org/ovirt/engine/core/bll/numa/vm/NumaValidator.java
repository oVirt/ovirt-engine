package org.ovirt.engine.core.bll.numa.vm;

import static java.lang.Integer.min;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class NumaValidator {

    /**
     * preferred supports single pinned vnuma node (without that VM fails to run in libvirt)
     */
    private static ValidationResult checkNumaPreferredTuneMode(NumaTuneMode numaTuneMode,
            List<VmNumaNode> vmNumaNodes) {
        // check tune mode
        if (numaTuneMode != NumaTuneMode.PREFERRED) {
            return ValidationResult.VALID;
        }

        // check single node pinned
        if (vmNumaNodes.size() == 1) {
            List<Pair<Guid, Pair<Boolean, Integer>>> vdsNumaNodeList = vmNumaNodes.get(0).getVdsNumaNodeList();
            boolean pinnedToSingleNode = vdsNumaNodeList != null
                    && vdsNumaNodeList.size() == 1
                    && vdsNumaNodeList.get(0).getSecond() != null
                    && vdsNumaNodeList.get(0).getSecond().getFirst();
            if (pinnedToSingleNode) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }

    /**
     * Check if we have enough virtual cpus for the virtual numa nodes
     *
     * @param numaNodeCount number of virtual numa nodes
     * @param cpuCores      number of virtual cpu cores
     * @return the validation result
     */
    private static ValidationResult checkVmNumaNodeCount(int numaNodeCount, int cpuCores) {

        if (cpuCores < numaNodeCount) {
            return new ValidationResult(EngineMessage.VM_NUMA_NODE_MORE_NODES_THAN_CPUS,
                    String.format("$numaNodes %d", numaNodeCount),
                    String.format("$cpus %d", cpuCores));
        }

        return ValidationResult.VALID;
    }

    /**
     * Check if the provided numa nodes do not containe the same numa node index more than once
     *
     * @param vmNumaNodes to check for duplicates
     * @return {@link ValidationResult#VALID} if no duplicates exist
     */
    public static ValidationResult checkVmNumaIndexDuplicates(final List<VmNumaNode> vmNumaNodes) {
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
     * Check if the indices of the provided numa nodes are continuous
     *
     * @param vmNumaNodes to check if indices are continuous
     * @return {@link ValidationResult#VALID} if no indices are missing
     */
    public static ValidationResult checkVmNumaIndexContinuity(final List<VmNumaNode> vmNumaNodes) {
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
     * Check if the numa configuration on the VM is consistent. This only checks the VM and the host pinning. No
     * compatibility checks regarding the host are performed
     *
     * @param vm to check
     * @return validation result
     */
    public static ValidationResult validateVmNumaConfig(final VM vm, final List<VmNumaNode> vmNumaNodes) {

        if (vmNumaNodes.isEmpty()) {
            return ValidationResult.VALID;
        }

        ValidationResult validationResult = checkNumaPreferredTuneMode(vm.getNumaTuneMode(),
                vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = checkVmNumaNodeCount(vmNumaNodes.size(), vm.getNumOfCpus());
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

        return ValidationResult.VALID;
    }

    /**
     * Check if a VM can run on specific hostNumaNodes with the provided numa configuration. The numa nodes for
     * validation need to be passed in separately because the numa nodes are not necessarily part of the VM when the
     * validation takes place.
     *
     * @param vm            with numa nodes
     * @param vmNumaNodes   to use for validation
     * @param hostNumaNodes from a host
     * @return weather the vm can run on the hostNumaNodes or not
     */
    public static ValidationResult validateNumaCompatibility(final VM vm, final List<VmNumaNode> vmNumaNodes, final
    List<VdsNumaNode>
            hostNumaNodes) {

        if (hostNumaNodes == null || hostNumaNodes.isEmpty()) {
            return new ValidationResult(EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
        }

        if (hostNumaNodes.size() == 1) { // One node is equal to no NUMA node architecture present
            return new ValidationResult(EngineMessage.HOST_NUMA_NOT_SUPPORTED);
        }

        final HashMap<Integer, VdsNumaNode> hostNodeMap = new HashMap<>();
        for (VdsNumaNode hostNumaNode : hostNumaNodes) {
            hostNodeMap.put(hostNumaNode.getIndex(), hostNumaNode);
        }
        boolean memStrict = vm.getNumaTuneMode() == NumaTuneMode.STRICT;
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            // Pair<vdsNumaNode#id, Pair<pinned, vdsNumaNode#index>>
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() == null || pair.getSecond().getSecond() == null) {
                    return new ValidationResult(EngineMessage.VM_NUMA_NODE_PINNED_INDEX_ERROR);
                }
                Integer hostNodeIndex = pair.getSecond().getSecond();
                if (pair.getSecond().getFirst() && !hostNodeMap.containsKey(hostNodeIndex)) {
                    return new ValidationResult(EngineMessage.VM_NUMA_NODE_HOST_NODE_INVALID_INDEX,
                            String.format("$vdsNodeIndex %d", hostNodeIndex));
                }
                if (memStrict) {
                    final VdsNumaNode hostNumaNode = hostNodeMap.get(hostNodeIndex);
                    if (vmNumaNode.getMemTotal() > hostNumaNode.getMemTotal()) {
                        return new ValidationResult(EngineMessage.VM_NUMA_NODE_MEMORY_ERROR);
                    }
                }
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Check if the VM pinning to the host is valid. By default this means a single host where it is pinned to
     *
     * @param vm to check
     * @return validation result
     */
    public static ValidationResult validateVmPinning(final VM vm) {

        //TODO Proper validation for multiple hosts for SupportNumaMigration was never implemented. Implement it.
        // validate - pinning is mandatory, since migration is not allowed
        if (vm.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST || vm.getDedicatedVmForVdsList()
                .isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
        }
        if (vm.getDedicatedVmForVdsList().size() > 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
        }
        return ValidationResult.VALID;
    }

    /**
     * Check the whole numa configuration of a VM. The numa nodes for validation need to be passed in separately because
     * the numa nodes are not necessarily part of the VM when the validation takes place.
     *
     * @param vm          to check comaptiblity with
     * @param vmNumaNodes to use for validation
     * @return the validation result
     */
    public static ValidationResult checkVmNumaNodesIntegrity(final VM vm, final List<VmNumaNode> vmNumaNodes) {
        if (vmNumaNodes.isEmpty()) {
            return ValidationResult.VALID;
        }

        ValidationResult validationResult = validateVmNumaConfig(vm, vmNumaNodes);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        //TODO Proper validation for multiple host pinning
        //TODO Numa sheduling policy
        if (Config.<Boolean>getValue(ConfigValues.SupportNUMAMigration)) {
            return ValidationResult.VALID;
        }

        validationResult = validateVmPinning(vm);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        final List<VdsNumaNode> hostNumaNodes = DbFacade.getInstance().getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(
                vm.getDedicatedVmForVdsList().get(0));
        return validateNumaCompatibility(vm, vmNumaNodes, hostNumaNodes);
    }

    private static String formatMissingIndices(List<Integer> missingIndices) {
        String str = StringUtils.join(missingIndices.subList(0, min(10, missingIndices.size())), ", ");
        if (missingIndices.size() > 10) {
            str = str + ", ...";
        }
        return str;
    }
}
