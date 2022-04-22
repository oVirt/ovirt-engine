package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuPinning;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "6d636bf6-a35c-4f9d-b68d-0731f720cddc",
        name = "CPU",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts with less CPUs than VM's CPUs")
public class CPUPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CPUPolicyUnit.class);

    @Inject
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    @Inject
    private ResourceManager resourceManager;

    public CPUPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Filters out the hosts that do not have enough free CPUs to accommodate the shared and exclusively pinned CPUs
     * required by the vmGroup.
     */
    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        List<VDS> candidates = new ArrayList<>();

        int maxVmGroupSharedCpuCount = countMaxVmGroupSharedCpuCount(vmGroup);

        for (VDS host : hosts) {

            if (host.getCpuSockets() == null || host.getCpuCores() == null || host.getCpuThreads() == null) {
                log.warn("Unknown number of cores for host {}.", host.getName());
                continue;
            }

            // total number of host CPUs (either core or threads based on countThreadsAsCores)
            Integer hostCpuCount =
                    SlaValidator.getEffectiveCpuCores(host, context.getCluster().getCountThreadsAsCores());

            // all CPUs (either core or threads based on countThreadsAsCores)
            // unavailable due to the exclusive pinning (counted for running VMs, pending, vmGroup)
            int exclusiveCpus = countExclusiveCPUs(host, vmGroup, context.getCluster().getCountThreadsAsCores());

            // the maximal number of CPU count for all VMs (running, pending, vmGroup)
            // it determines how many shared CPUs needs to be available on the host
            long maxSharedCpuCount = Math.max(countMaxRunningVmsSharedCpuCount(host), maxVmGroupSharedCpuCount);

            if (hostCpuCount - exclusiveCpus - maxSharedCpuCount < 0) {
                messageNotEnoughCores(host, messages);
                continue;
            }

            candidates.add(host);
        }
        return candidates;
    }

    /**
     * Counts how many CPUs will be unavailable due to the exclusive pinning. The method
     * takes a copy of the current CPU topology, applies pending resources and pins all of the exclusively
     * pinned VMs. Then counts either the unavailable threads or the whole cores based on the countThreadsAsCores
     * parameter.
     * @param host Host candidate
     * @param vmGroup VMs to be scheduled
     * @param countThreadsAsCores If the threads should be counted as cores
     * @return Number of CPUs that are exclusively pinned or blocked by an exclusive pinning or Integer.MAX_VALUE
     * if the pinning of the exclusive CPUs is not possible (shared pinnings should be checked in {@link CpuPinningPolicyUnit}
     */
    private int countExclusiveCPUs(VDS host, List<VM> vmGroup, boolean countThreadsAsCores) {

        List<VdsCpuUnit> cpuTopology = getEffectiveCpuTopology(host);

        if (cpuTopology.isEmpty()) {
            return 0;
        }

        for (VM vm : vmGroup) {
            if (!host.getId().equals(vm.getRunOnVds())) {
                List<VdsCpuUnit> allocatedCpus =
                        vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, cpuTopology, host.getId());
                if (vm.getCpuPinningPolicy().isExclusive() && (allocatedCpus == null || allocatedCpus.isEmpty())) {
                    return Integer.MAX_VALUE;
                }
            }
        }

        return vdsCpuUnitPinningHelper.countUnavailableCpus(
                cpuTopology,
                countThreadsAsCores);
    }

    private List<VdsCpuUnit> getEffectiveCpuTopology(VDS host) {
        List<VdsCpuUnit> cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();

        Map<Guid, List<VdsCpuUnit>> vmToPendingExclusiveCpuPinnings =
                PendingCpuPinning.collectForHost(getPendingResourceManager(), host.getId());

        vdsCpuUnitPinningHelper.previewPinOfPendingExclusiveCpus(cpuTopology, vmToPendingExclusiveCpuPinnings);
        return cpuTopology;
    }

    /**
     * Finds the VM with the maximum number of shared CPUs and returns the CPU count
     * @param vmGroup VMs to be scheduled
     * @return Max count of shared CPUs in the vm group
     */
    private int countMaxVmGroupSharedCpuCount(List<VM> vmGroup) {
        // the number of host's shared CPUs has to >= as the number of CPUs of any VM
        int maxVmGroupSharedCpuCount = vmGroup.stream()
                .filter(vm -> !vm.getCpuPinningPolicy().isExclusive())
                .mapToInt(vm -> VmCpuCountHelper.getDynamicNumOfCpu(vm))
                .max()
                .orElse(0);
        return maxVmGroupSharedCpuCount;
    }

    /**
     * Checks running VMs and pending resources and finds the VM with the maximum number
     * of shared CPU and returns the CPU count
     * @param host Host candidate
     * @return Max count of shared CPUs in running VMs and pending resources
     */
    private long countMaxRunningVmsSharedCpuCount(VDS host) {

        long maxPendingSharedCount = pendingResourceManager.pendingHostResources(host.getId(), PendingCpuCores.class)
                .stream()
                .filter(pending -> !pending.getCpuPinningPolicy().isExclusive())
                .mapToLong(pending -> pending.getCoreCount())
                .max()
                .orElse(0);

        long maxRunningSharedCpuCount = resourceManager.getVdsManager(host.getId()).getMinRequiredSharedCpusCount();

        return Math.max(maxPendingSharedCount, maxRunningSharedCpuCount);
    }

    private void messageNotEnoughCores(VDS vds, PerHostMessages messages) {
        messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_CORES.toString());
        log.debug("Host '{}' has not enough available cores to schedule vms)",
                vds.getName());
    }
}
