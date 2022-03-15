package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collection;
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
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
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
            final VM vm,
            final PerHostMessages messages) {
        final String cpuPinning = VmCpuCountHelper.isDynamicCpuPinning(vm) ? vm.getCurrentCpuPinning() : vm.getCpuPinning();

        // return all hosts when no CPU pinning is requested
        if (vm.getCpuPinningPolicy() == CpuPinningPolicy.NONE) {
            return hosts;
        }


        // only add hosts as candidates which have all required CPUs up and running
        final List<VDS> candidates = new ArrayList<>();

        switch (vm.getCpuPinningPolicy()) {
            case MANUAL:
            case RESIZE_AND_PIN_NUMA:
                if (StringUtils.isEmpty(cpuPinning)) {
                    // The logic below applies to MigrateVm in which we get here with CPU pinning that was set on the
                    // source host. On RunVm, we get here before determining the CPU pinning for the VM so the dynamic
                    // CPU pinning is not set and hosts should not be filtered by the logic below.
                    return hosts;
                }
                // collect all pinned host cpus and merge them into one set
                final Set<Integer> pinnedCpus = CpuPinningHelper.getAllPinnedPCpus(cpuPinning);
                for (final VDS host : hosts) {
                    final Collection<Integer> onlineHostCpus = SlaValidator.getOnlineCpus(host);
                    var cpuTopology = resourceManager.getVdsManager(host.getId()).getCpuTopology();
                    final Collection<Integer> dedicatedCpus = cpuTopology.stream().filter(VdsCpuUnit::isDedicated).map(VdsCpuUnit::getCpu).collect(Collectors.toList());
                    final Collection availableCpus = CollectionUtils.subtract(onlineHostCpus, dedicatedCpus);
                    final Collection difference = CollectionUtils.subtract(pinnedCpus, availableCpus);
                    if (difference.isEmpty()) {
                        candidates.add(host);
                    } else {
                        messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__VM_PINNING_PCPU_DOES_NOT_EXIST.name());
                        messages.addMessage(host.getId(), String.format("$missingCores %1$s", StringUtils.join(difference, ", ")));
                        log.debug("Host {} does not satisfy the cpu pinning constraints because of missing or offline cpus {}.",
                                host.getId(),
                                StringUtils.join(difference, ", "));
                    }
                }
                break;
            case DEDICATED:
                for (final VDS host : hosts) {
                    if (host.getCpuTopology() == null || host.getCpuTopology().isEmpty()) {
                        // means CPU topology not reported for this host, lets ignore it
                        messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NO_HOST_CPU_DATA.name());
                        log.debug("Host {} does not have the cpu topology data.", host.getId());
                        continue;
                    }

                    Map<Guid, List<VdsCpuUnit>> vmToPendingDedicatedCpuPinnings =
                            PendingCpuPinning.collectForHost(getPendingResourceManager(), host.getId());
                    boolean isDedicatedCpuPinningPossibleAtHost =
                            vdsCpuUnitPinningHelper.isDedicatedCpuPinningPossibleAtHost(
                                    vmToPendingDedicatedCpuPinnings,
                                    vm,
                                    host);
                    if (isDedicatedCpuPinningPossibleAtHost) {
                        candidates.add(host);
                    } else {
                        messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__VM_PINNING_DEDICATED_NOT_FIT.name());
                        log.debug("Host {} does not satisfy CPU pinning constraints, cannot match virtual topology " +
                                "with available CPUs.", host.getId());
                    }
                }
                break;
        }

        return candidates;
    }
}
