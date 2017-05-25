package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingOvercommitMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
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

    @Inject
    SlaValidator slaValidator;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;
    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    public MemoryPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        // If Vm in Paused mode - no additional memory allocation needed
        if (vm.getStatus() == VMStatus.Paused) {
            return hosts;
        }
        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());
        boolean vmNumaPinned = isVmNumaPinned(vmNumaNodes);

        List<VDS> filteredList = new ArrayList<>();
        for (VDS vds : hosts) {
            // Check physical memory needed to start / receive the VM
            // This is probably not needed for all VMs, but QEMU might attempt full
            // allocation without provoked and fail if there is not enough memory
            int pendingRealMemory = PendingMemory.collectForHost(getPendingResourceManager(), vds.getId());

            if (!slaValidator.hasPhysMemoryToRunVM(vds, vm, pendingRealMemory)) {
                Long hostAvailableMem = vds.getMemFree() + vds.getSwapFree();
                log.debug(
                        "Host '{}' has insufficient memory to run the VM. Only {} MB of physical memory + swap are available.",
                        vds.getName(),
                        hostAvailableMem);

                messages.addMessage(vds.getId(), String.format("$availableMem %1$d", hostAvailableMem));
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_MEMORY.toString());
                continue;
            }

            // In case one of VM's virtual NUMA nodes (vNode) is pinned to physical NUMA nodes (pNode),
            // host will be excluded ('filter out') when:
            // * memory tune is strict (vNode memory cannot be spread across several pNodes' memory)
            // [and]
            // * host support NUMA configuration
            // * there isn't enough memory for pinned vNode in pNode
            if (vm.getNumaTuneMode() == NumaTuneMode.STRICT && vmNumaPinned
                    && (!vds.isNumaSupport() || !canVmNumaPinnedToVds(vm, vmNumaNodes, vds))) {
                log.debug("Host '{}' cannot accommodate memory of VM's pinned virtual NUMA nodes within host's physical NUMA nodes",
                        vds.getName());
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_MEMORY_PINNED_NUMA.toString());
                continue;
            }
            filteredList.add(vds);
        }

        List<VDS> resultList = new ArrayList<>();
        List<VDS> overcommitFailed = new ArrayList<>();

        boolean canDelay = false;
        for (VDS vds : filteredList) {
            // Only delay if there are pending VMs to run.
            // Otherwise, pending memory will not change by waiting.
            if (vds.getPendingVmemSize() > 0) {
                canDelay = true;
            }

            // Check logical memory using overcommit, pending and guaranteed memory rules
            if (slaValidator.hasOvercommitMemoryToRunVM(vds, vm)) {
                resultList.add(vds);
            } else {
                overcommitFailed.add(vds);
            }
        }

        // Wait a while, to see if pending memory was freed on some host
        if (canDelay && resultList.isEmpty()) {
            log.debug("Not enough memory on hosts. Delaying...");

            overcommitFailed.clear();

            runVmDelayer.delay(filteredList.stream()
                    .map(VDS::getId)
                    .collect(Collectors.toList()));

            for (VDS vds : filteredList) {
                // Refresh pending memory
                int pendingMemory = PendingOvercommitMemory.collectForHost(getPendingResourceManager(), vds.getId());
                vds.setPendingVmemSize(pendingMemory);

                // Check logical memory using overcommit, pending and guaranteed memory rules
                if (slaValidator.hasOvercommitMemoryToRunVM(vds, vm)) {
                    resultList.add(vds);
                } else {
                    overcommitFailed.add(vds);
                }
            }
        }

        for (VDS vds : overcommitFailed) {
            log.debug("Host '{}' is already too close to the memory overcommitment limit. It can only accept {} MB of additional memory load.",
                    vds.getName(),
                    vds.getMaxSchedulingMemory());

            messages.addMessage(vds.getId(), String.format("$availableMem %1$.0f", vds.getMaxSchedulingMemory()));
            messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_MEMORY.toString());
        }

        return resultList;
    }

    private boolean canVmNumaPinnedToVds(VM vm, List<VmNumaNode> nodes, VDS vds) {
        List<VdsNumaNode> pNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vds.getId());
        if (pNodes == null || pNodes.isEmpty()) {
            return false;
        }
        Map<Integer, VdsNumaNode> indexMap = toMap(pNodes);
        for (VmNumaNode vNode : nodes) {
            for (Integer pinnedIndex : vNode.getVdsNumaNodeList()) {
                if (vNode.getMemTotal() > indexMap.get(pinnedIndex)
                        .getNumaNodeStatistics()
                        .getMemFree()) {
                    return false;
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
            if (!vmNumaNode.getVdsNumaNodeList().isEmpty()) {
                return true;
            }
        }
        return false;
    }


}
