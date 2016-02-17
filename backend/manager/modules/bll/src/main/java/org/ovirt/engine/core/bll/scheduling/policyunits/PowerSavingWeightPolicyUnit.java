package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class PowerSavingWeightPolicyUnit extends EvenDistributionWeightPolicyUnit {

    public PowerSavingWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            int score = MaxSchedulerWeight - 1;
            if (vds.getVmCount() > 0) {
                score -= calcEvenDistributionScore(vds, vm, cluster.getCountThreadsAsCores());
            }
            scores.add(new Pair<>(vds.getId(), score));
        }
        return scores;
    }
}
