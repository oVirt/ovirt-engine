package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483edb",
        name = "OptimalForEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description = "Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more"
                + " likely to be selected)"
)
public class EvenDistributionWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Calculate a single host weight score according to various parameters.
     * @param hostCores - threads/cores according to cluster
     */
    private double calcDistributeMetric(VDS vds, VM vm, int hostCores) {
        int vcpu = Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> getValue(ConfigValues.SpmVCpuConsumption);
        double hostCpu = vds.getUsageCpuPercent();
        double pendingVcpus = PendingCpuCores.collectForHost(getPendingResourceManager(), vds.getId());

        return (hostCpu / vcpu) + (pendingVcpus + vm.getNumOfCpus() + spmCpu) / hostCores;
    }

    protected int calcEvenDistributionScore(VDS vds, VM vm, boolean countThreadsAsCores) {
        int score = MaxSchedulerWeight - 1;
        Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
        if (effectiveCpuCores != null
                && vds.getUsageCpuPercent() != null) {
            // round the result and adding one to avoid zero
            score = Math.min((int) Math.round(
                    calcDistributeMetric(vds, vm, effectiveCpuCores)) + 1, MaxSchedulerWeight);
        }

        // TODO Each 100 MB of free memory is equal to 1% of free CPU
        score -= vds.getMaxSchedulingMemory() / 100;

        return score;
    }

    @Override
    public List<Pair<Guid, Integer>> score(List<VDS> hosts, VM vm, Map<String, String> parameters) {
        Cluster cluster = DbFacade.getInstance().getClusterDao().get(hosts.get(0).getClusterId());
        boolean countThreadsAsCores = cluster != null ? cluster.getCountThreadsAsCores() : false;
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcEvenDistributionScore(vds, vm, countThreadsAsCores)));
        }
        return scores;
    }

}
