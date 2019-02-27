package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
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
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        List<VM> vmsToCheck = vmGroup.stream()
                // If Vm in Paused mode - no additional memory allocation needed
                .filter(vm -> vm.getStatus() != VMStatus.Paused)
                .collect(Collectors.toList());

        if (vmGroup.isEmpty()) {
            return hosts;
        }

        List<VDS> filteredList = new ArrayList<>();
        for (VDS vds : hosts) {
            // Skip checks if the VM is currently running on the host
            List<VM> vmsNotRunningOnHost = vmsToCheck.stream()
                    .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                    .collect(Collectors.toList());

            if (vmsNotRunningOnHost.isEmpty()) {
                filteredList.add(vds);
                continue;
            }

            // Check physical memory needed to start / receive the VM
            // This is probably not needed for all VMs, but QEMU might attempt full
            // allocation without provoked and fail if there is not enough memory
            int pendingRealMemory = PendingMemory.collectForHost(getPendingResourceManager(), vds.getId());

            if (!slaValidator.hasPhysMemoryToRunVmGroup(vds, vmsNotRunningOnHost, pendingRealMemory)) {
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

            List<VM> vmsNotRunningOnHost = vmsToCheck.stream()
                    .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                    .collect(Collectors.toList());

            // Check logical memory using overcommit, pending and guaranteed memory rules
            if (slaValidator.hasOvercommitMemoryToRunVM(vds, vmsNotRunningOnHost)) {
                resultList.add(vds);
            } else {
                overcommitFailed.add(vds);
            }
        }

        // Wait a while and restart the scheduling
        if (context.isCanDelay() && !context.isShouldDelay() && canDelay && resultList.isEmpty()) {
            context.setShouldDelay(true);
            return Collections.emptyList();
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
