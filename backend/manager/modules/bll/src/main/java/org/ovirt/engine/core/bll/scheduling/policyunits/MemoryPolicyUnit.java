package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "c9ddbb34-0e1d-4061-a8d7-b0893fa80932",
        name = "Memory",
        description = "Filters out hosts that have insufficient memory to run the VM",
        type = PolicyUnitType.FILTER
)
public class MemoryPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(MemoryPolicyUnit.class);

    public MemoryPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        List<VDS> list = new ArrayList<>();
        // If Vm in Paused mode - no additional memory allocation needed
        if (vm.getStatus() == VMStatus.Paused) {
            return hosts;
        }
        List<VmNumaNode> vmNumaNodes = DbFacade.getInstance().getVmNumaNodeDao().getAllVmNumaNodeByVmId(vm.getId());
        for (VDS vds : hosts) {
            if (!isVMSwapValueLegal(vds)) {
                log.debug("Host '{}' swap value is illegal", vds.getName());
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__SWAP_VALUE_ILLEGAL.toString());
                continue;
            }

            // Check physical memory needed to start / receive the VM
            // This is probably not needed for all VMs, but QEMU might attempt full
            // allocation without provoked and fail if there is not enough memory
            int pendingRealMemory = PendingMemory.collectForHost(getPendingResourceManager(), vds.getId());

            if (!SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, pendingRealMemory)) {
                Long hostAvailableMem = vds.getMemFree() + vds.getSwapFree();
                log.debug(
                        "Host '{}' has insufficient memory to run the VM. Only {} MB of physical memory + swap are available.",
                        vds.getName(),
                        hostAvailableMem);

                messages.addMessage(vds.getId(), String.format("$availableMem %1$d", hostAvailableMem));
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_MEMORY.toString());
                continue;
            }

            // Check logical memory using overcommit, pending and guaranteed memory rules
            if (!memoryChecker.evaluate(vds, vm)) {
                log.debug("Host '{}' is already too close to the memory overcommitment limit. It can only accept {} MB of additional memory load.",
                        vds.getName(),
                        vds.getMaxSchedulingMemory());

                messages.addMessage(vds.getId(), String.format("$availableMem %1$f", vds.getMaxSchedulingMemory()));
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_MEMORY.toString());
                continue;
            }

            // In case one of VM's virtual NUMA nodes (vNode) is pinned to physical NUMA nodes (pNode),
            // host will be excluded ('filter out') when:
            // * memory tune is strict (vNode memory cannot be spread across several pNodes' memory)
            // [and]
            // * host support NUMA configuration
            // * there isn't enough memory for pinned vNode in pNode
            if (vm.getNumaTuneMode() == NumaTuneMode.STRICT && isVmNumaPinned(vmNumaNodes)
                    && (!vds.isNumaSupport() || !canVmNumaPinnedToVds(vm, vmNumaNodes, vds))) {
                log.debug("Host '{}' cannot accommodate memory of VM's pinned virtual NUMA nodes within host's physical NUMA nodes",
                        vds.getName());
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_MEMORY_PINNED_NUMA.toString());
                continue;
            }
            list.add(vds);
        }
        return list;
    }

    private boolean canVmNumaPinnedToVds(VM vm, List<VmNumaNode> nodes, VDS vds) {
        List<VdsNumaNode> pNodes = DbFacade.getInstance().getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vds.getId());
        if (pNodes == null || pNodes.isEmpty()) {
            return false;
        }
        Map<Integer, VdsNumaNode> indexMap = toMap(pNodes);
        for (VmNumaNode vNode : nodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vNode.getVdsNumaNodeList()) {
                if (pair.getSecond() != null && pair.getSecond().getFirst()) {
                    if (vNode.getMemTotal() > indexMap.get(pair.getSecond().getSecond())
                            .getNumaNodeStatistics()
                            .getMemFree()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Map<Integer, VdsNumaNode> toMap(List<VdsNumaNode> pNodes) {
        Map<Integer, VdsNumaNode> map = new HashMap<>();
        for (VdsNumaNode pNode : pNodes) {
            map.put(pNode.getIndex(), pNode);
        }
        return map;
    }

    private boolean isVmNumaPinned(List<VmNumaNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        // iterate through the nodes, and see if there's at least one pinned node.
        for (VmNumaNode vmNumaNode : nodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() != null && pair.getSecond().getFirst()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether [is VM swap value legal] [the specified VDS].
     * @param host
     *            The VDS.
     * @return <c>true</c> if [is VM swap value legal] [the specified VDS]; otherwise, <c>false</c>.
     */
    private boolean isVMSwapValueLegal(VDS host) {
        if (!Config.<Boolean> getValue(ConfigValues.EnableSwapCheck)) {
            return true;
        }

        if (host.getSwapTotal() == null || host.getSwapFree() == null || host.getMemAvailable() == null
                || host.getMemAvailable() <= 0 || host.getPhysicalMemMb() == null || host.getPhysicalMemMb() <= 0) {
            return true;
        }

        long swap_total = host.getSwapTotal();
        long swap_free = host.getSwapFree();
        long mem_available = host.getMemAvailable();
        long physical_mem_mb = host.getPhysicalMemMb();

        return ((swap_total - swap_free - mem_available) * 100 / physical_mem_mb) <= Config
                .<Integer> getValue(ConfigValues.BlockMigrationOnSwapUsagePercentage);
    }
}
