package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
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
}
