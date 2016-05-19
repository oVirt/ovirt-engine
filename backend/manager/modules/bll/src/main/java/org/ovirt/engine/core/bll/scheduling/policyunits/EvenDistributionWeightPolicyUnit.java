package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public abstract class EvenDistributionWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        float maxMemoryOfVdsInCluster = getMaxMemoryOfVdsInCluster(hosts);
        boolean countThreadsAsCores = cluster.getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(),
                    calcEvenDistributionScore(maxMemoryOfVdsInCluster, vds, vm, countThreadsAsCores)));
        }
        return scores;
    }

    public List<Pair<Guid, Integer>> reverseEvenDistributionScore(Cluster cluster,
            List<VDS> hosts,
            VM vm,
            Map<String, String> parameters) {
        float maxMemoryOfVdsInCluster = getMaxMemoryOfVdsInCluster(hosts);
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            int score = MaxSchedulerWeight - 1;
            if (vds.getVmCount() > 0) {
                score -= calcEvenDistributionScore(maxMemoryOfVdsInCluster, vds, vm, cluster.getCountThreadsAsCores());
            }
            scores.add(new Pair<>(vds.getId(), score));
        }
        return scores;
    }

    protected float getMaxMemoryOfVdsInCluster(List<VDS> hosts) {
        return hosts.stream().map(VDS::getFreeVirtualMemory).max(Float::compareTo).get();
    }

    /**
     * Calculate a single host weight score according to various parameters.
     *
     * @param vds                     host on which the score is calculated for
     * @param vm                      virtual machine to be deployed at a selected host by score
     * @param countThreadsAsCores     true - count threads as cores , false - otherwise
     * @param maxMemoryOfVdsInCluster maximum available memory for scheduling of a vds from all the available hosts
     * @return weight score for a single host
     */
    protected abstract int calcEvenDistributionScore(float maxMemoryOfVdsInCluster,
            VDS vds,
            VM vm,
            boolean countThreadsAsCores);
}
