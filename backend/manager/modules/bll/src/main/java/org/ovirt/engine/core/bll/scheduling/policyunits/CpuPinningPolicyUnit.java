package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy unit which checks if provided hosts satisfy the cpu pinning requirements.
 */
public class CpuPinningPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(CpuPinningPolicyUnit.class);

    public CpuPinningPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(VDSGroup cluster, final List<VDS> hosts,
            final VM vm,
            final Map<String, String> parameters,
            final PerHostMessages messages) {
        final String cpuPinning = vm.getCpuPinning();

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
                messages.addMessages(host.getId(), ReplacementUtils.replaceWith("missingCores", difference, ", ", 10));
                log.debug("Host {} does not satisfy the cpu pinning constraints because of missing or offline cpus {}.",
                        host.getId(),
                        StringUtils.join(difference, ", "));
            }
        }
        return candidates;
    }
}
