package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "98e92667-6161-41fb-b3fa-34f820ccbc4b",
        name = "HA",
        description = "Weights hosts according to their HA score",
        type = PolicyUnitType.WEIGHT
)
public class HostedEngineHAClusterWeightPolicyUnit extends PolicyUnitImpl {
    private static int DEFAULT_WEIGHT = 1;
    private static int MAXIMUM_HA_SCORE = 2400;

    public HostedEngineHAClusterWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    void fillDefaultScores(List<VDS> hosts, List<Pair<Guid, Integer>> scores) {
        for (VDS host : hosts) {
            scores.add(new Pair<>(host.getId(), DEFAULT_WEIGHT));
        }
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        boolean isHostedEngine = vm.isHostedEngine();

        if (isHostedEngine) {
            // If the max HA score is higher than the max weight, then we normalize. Otherwise the ratio is 1, keeping the value as is
            float ratio = MAXIMUM_HA_SCORE > MaxSchedulerWeight ? ((float) MaxSchedulerWeight / MAXIMUM_HA_SCORE) : 1;
            for (VDS host : hosts) {
                scores.add(new Pair<>(host.getId(), MaxSchedulerWeight - Math.round(host.getHighlyAvailableScore() * ratio)));
            }
        } else {
            fillDefaultScores(hosts, scores);
        }
        return scores;
    }

}
