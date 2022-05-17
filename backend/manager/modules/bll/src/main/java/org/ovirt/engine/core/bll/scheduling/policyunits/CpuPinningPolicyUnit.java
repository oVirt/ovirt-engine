package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuPinning;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.CpuPinningHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy unit which checks if provided hosts satisfy the cpu pinning requirements.
 */
@SchedulingUnit(
        guid = "6d636bf6-a35c-4f9d-b68d-0731f731cddc",
        name = "CpuPinning",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts which do not satisfy a VMs cpu pinning constraints"
)
public class CpuPinningPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(CpuPinningPolicyUnit.class);

    @Inject
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;
    @Inject
    private ResourceManager resourceManager;

    public CpuPinningPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(final SchedulingContext context,
            final List<VDS> hosts,
            final List<VM> vmGroup,
            final PerHostMessages messages) {

        // return all hosts when no CPU pinning is requested
        if (vmGroup.stream().allMatch(vm -> vm.getCpuPinningPolicy() == CpuPinningPolicy.NONE)) {
            return hosts;
        }

        // only add hosts as candidates which have all required CPUs up and running
        final List<VDS> candidates = new ArrayList<>();

        List<VM> exclusiveVms = vmGroup.stream()
                .filter(vm -> vm.getCpuPinningPolicy().isExclusive())
                .collect(Collectors.toList());
        List<VM> sharedVms = vmGroup.stream()
                // For 'Resize and Pin NUMA', on RunVm, we get here before determining the CPU pinning for the VM
                // so the dynamic CPU pinning is not set and hosts should not be filtered by the logic below.
                .filter(vm -> !vm.getCpuPinningPolicy().isExclusive() && !StringUtils.isEmpty(vm.getVmPinning()))
                .collect(Collectors.toList());

        for (final VDS host : hosts) {
            var cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();
            Map<Guid, List<VdsCpuUnit>> vmToPendingDedicatedCpuPinnings =
                    PendingCpuPinning.collectForHost(getPendingResourceManager(), host.getId());
            vdsCpuUnitPinningHelper.previewPinOfPendingExclusiveCpus(cpuTopology, vmToPendingDedicatedCpuPinnings);
            allocateSharedVms(cpuTopology, sharedVms);

            if (!exclusiveVms.isEmpty()) {
                if (cpuTopology == null || cpuTopology.isEmpty()) {
                    // means CPU topology not reported for this host, lets ignore it
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NO_HOST_CPU_DATA.name());
                    log.debug("Host {} does not have the cpu topology data.", host.getId());
                    continue;
                }
                boolean isDedicatedCpuPinningPossibleAtHost =
                        vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(
                                vmToPendingDedicatedCpuPinnings,
                                vmGroup,
                                host.getId(),
                                cpuTopology);
                if (!isDedicatedCpuPinningPossibleAtHost) {
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__VM_PINNING_DEDICATED_NOT_FIT.name());
                    log.debug("Host {} does not satisfy CPU pinning constraints, cannot match virtual topology " +
                            "with available CPUs.", host.getId());
                    continue;
                }
            }
            if (isResizeAndExclusive(cpuTopology, vmGroup, exclusiveVms)) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__VM_PINNING_CANT_RESIZE_WITH_DEDICATED.name());
                log.debug("Host {} does not satisfy CPU pinning constraints, cannot match virtual topology " +
                        "with available CPUs due to exclusively pinned cpus on the host .", host.getId());
                continue;
            }

            // shared CPUs
            Set<Integer> pinnedCpus = new LinkedHashSet<>();
            // collect all pinned host cpus and merge them into one set
            sharedVms.forEach(vm -> pinnedCpus.addAll(CpuPinningHelper.getAllPinnedPCpus(vm.getCpuPinning())));
            final Collection<Integer> onlineHostCpus = SlaValidator.getOnlineCpus(host);
            final Collection<Integer> dedicatedCpus = getExclusivelyPinnedCpus(cpuTopology);

            final Collection availableCpus = CollectionUtils.subtract(onlineHostCpus, dedicatedCpus);
            final Collection difference = CollectionUtils.subtract(pinnedCpus, availableCpus);
            if (!difference.isEmpty()) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__VM_PINNING_PCPU_DOES_NOT_EXIST.name());
                messages.addMessage(host.getId(), String.format("$missingCores %1$s", StringUtils.join(difference, ", ")));
                log.debug("Host {} does not satisfy the cpu pinning constraints because of missing, exclusively pinned or offline cpus {}.",
                        host.getId(),
                        StringUtils.join(difference, ", "));
                continue;
            }
            candidates.add(host);
        }
        return candidates;
    }

    private Collection<Integer> getExclusivelyPinnedCpus(List<VdsCpuUnit> cpuTopology) {
        return cpuTopology.stream()
                .filter(VdsCpuUnit::isExclusive)
                .map(VdsCpuUnit::getCpu)
                .collect(Collectors.toList());
    }

    private void allocateSharedVms(List<VdsCpuUnit> cpuTopology, List<VM> sharedVms) {
        sharedVms.forEach(vm -> vdsCpuUnitPinningHelper.allocateManualCpus(cpuTopology, vm));
    }

    private boolean isResizeAndExclusive(List<VdsCpuUnit> cpuTopology, List<VM> vmGroup, List<VM> exclusiveVms) {
        boolean isAnyVmResizeAndPin = vmGroup.stream()
                .anyMatch(vm -> vm.getCpuPinningPolicy() == CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        if (!exclusiveVms.isEmpty() && isAnyVmResizeAndPin) {
            return true;
        }
        if (!getExclusivelyPinnedCpus(cpuTopology).isEmpty() && isAnyVmResizeAndPin) {
            return true;
        }
        return false;
    }
}
