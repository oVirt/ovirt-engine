package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        guid = "4134247a-9c58-4b9a-8593-530bb9e37c59",
        name = "OptimalForMemoryEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description =
                "Gives hosts with higher available memory, lower weight (means that hosts with more available memory are more"
                        + " likely to be selected)"
)
public class EvenDistributionMemoryWeightPolicyUnit extends EvenDistributionWeightPolicyUnit {

    public EvenDistributionMemoryWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        float maxMemoryOfVdsInCluster = getMaxMemoryOfVdsInCluster(hosts);
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcEvenDistributionScore(maxMemoryOfVdsInCluster, vds, vm, false)));
        }
        return scores;
    }

    @Override
    protected int calcEvenDistributionScore(float maxMemoryOfVdsInCluster,
            VDS vds,
            VM vm,
            boolean countThreadsAsCores) {
        int score = Math.round(((vds.getMaxSchedulingMemory() - 1) * (MaxSchedulerWeight - 1))
                / (maxMemoryOfVdsInCluster - 1));
        return MaxSchedulerWeight - score;
    }

}
