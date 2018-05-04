package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "427aed70-dae3-48ba-8fe9-a902a9d563c8",
        name = "VmToHostsAffinityGroups",
        description = "Enables Affinity Groups soft enforcement for VMs to hosts;"
                + " VMs in group are most likely to run either on one of the hosts in group (positive)"
                + " or on independent hosts which are excluded from the hosts in group (negative)",
        type = PolicyUnitType.WEIGHT
)
public class VmToHostAffinityWeightPolicyUnit extends VmToHostAffinityPolicyUnit {

    private static final int DEFAULT_SCORE = 1;

    public VmToHostAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {

        Map<Guid, Integer> hostViolations =
                getHostViolationCount(false, hosts, vm, new PerHostMessages());

        Guid currentHostId = vm.getRunOnVds();
        int currentHostScore = hostViolations.getOrDefault(currentHostId, getMaxSchedulerWeight());

        List<Pair<Guid, Integer>> retList = new ArrayList<>();
        for (VDS host : hosts) {
            int score = hostViolations.getOrDefault(host.getId(), DEFAULT_SCORE);

            // Increase the score of hosts with equal or worse score
            // than the host where the VM is currently running.
            // If more hosts have the same number of violations,
            // the host where the VM is running is preferred.
            if (score >= currentHostScore && !host.getId().equals(currentHostId)) {
                score += 1;
            }

            retList.add(new Pair<>(host.getId(), Math.min(score, getMaxSchedulerWeight())));
        }

        return retList;
    }
}
