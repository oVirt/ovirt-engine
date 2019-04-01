package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmToHostAffinityPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(VmToHostAffinityPolicyUnit.class);

    @Inject
    AffinityGroupDao affinityGroupDao;

    public VmToHostAffinityPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Calculate and return the number of affinity group violations
     * for each host that would happen if the vm was started on it.
     *
     * @param enforcing true - hard constraint / false - soft constraint
     * @param hosts     list of available hosts in the cluster
     * @param vm        current vm targeted for migration
     * @param messages  log messages
     * @return map of hosts with affinity group violations count per host id.
     */
    public Map<Guid, Integer> getHostViolationCount(boolean enforcing,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {

        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsByVmId(vm.getId());
        // no affinity groups found for VM return all hosts with no violations
        if (affinityGroups.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Guid, Integer> hostViolations = new HashMap<>();
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isVdsEnforcing() == enforcing && affinityGroup.isVdsAffinityEnabled()) {

                List<Guid> vdsIds = affinityGroup.getVdsIds();
                if (affinityGroup.isVdsPositive()) {
                    // log and score hosts that violate the positive affinity rules
                    hosts.stream()
                            .filter(host -> !vdsIds.contains(host.getId()))
                            .forEach(host -> {
                                // TODO compute the affinity rule names
                                messages.addMessage(host.getId(), String.format("$affinityRules %1$s", ""));
                                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
                                hostViolations.merge(host.getId(), 1, Integer::sum);
                            });
                } else {
                    // log and score hosts that violate the negative affinity rules
                    hosts.stream()
                            .filter(host -> vdsIds.contains(host.getId()))
                            .forEach(host -> {
                                // TODO compute the affinity rule names
                                messages.addMessage(host.getId(), String.format("$affinityRules %1$s", ""));
                                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_NEGATIVE.toString());
                                hostViolations.merge(host.getId(), 1, Integer::sum);
                            });
                }
            }
        }

        return hostViolations;
    }
}
