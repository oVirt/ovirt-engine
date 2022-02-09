package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
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

    public CpuPinningPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(final SchedulingContext context,
            final List<VDS> hosts,
            final VM vm,
            final PerHostMessages messages) {
        final String cpuPinning = VmCpuCountHelper.isAutoPinning(vm) ? vm.getCurrentCpuPinning() : vm.getCpuPinning();

        // return all hosts when no host pinning is requested
        if (StringUtils.isEmpty(cpuPinning)) {
            return hosts;
        }

        // collect all pinned host cpus and merge them into one set
        final Set<Integer> pinnedCpus = CpuPinningHelper.getAllPinnedPCpus(cpuPinning);

        // only add hosts as candidates which have all required CPUs up and running
        final List<VDS> candidates = new ArrayList<>();
        for (final VDS host : hosts) {
            final Collection<Integer> onlineHostCpus = SlaValidator.getOnlineCpus(host);
            final Collection difference = CollectionUtils.subtract(pinnedCpus, onlineHostCpus);
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
        return candidates;
    }
}
