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
        guid = "4134247a-9c58-4b9a-8593-530bb9e37c59",
        name = "OptimalForMemoryEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description =
                "Gives hosts with higher available memory, lower weight (means that hosts with more available memory are more"
                        + " likely to be selected)"
)
public class EvenDistributionMemoryWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionMemoryWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        float maxMemoryOfVdsInCluster = getMaxMemoryOfVdsInCluster(hosts);
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcHostScore(maxMemoryOfVdsInCluster, vds)));
        }
        return scores;
    }

    /**
     * Calculate a single host weight score according to various parameters.
     *
     * @param vds                     host on which the score is calculated for
     * @param maxMemoryOfVdsInCluster maximum available memory for scheduling of a vds from all the available hosts
     * @return weight score for a single host
     */
    private int calcHostScore(float maxMemoryOfVdsInCluster, VDS vds) {
        int score = Math.round(((vds.getMaxSchedulingMemory() - 1) * (getMaxSchedulerWeight() - 1))
                / (maxMemoryOfVdsInCluster - 1));
        return getMaxSchedulerWeight() - score;
    }

    private float getMaxMemoryOfVdsInCluster(List<VDS> hosts) {
        return hosts.stream().map(VDS::getFreeVirtualMemory).max(Float::compareTo).get();
    }
}
