package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class EvenDistributionWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Calculate a single host weight score according to various parameters.
     * @param vds
     * @param vm
     * @param hostCores - threads/cores according to cluster
     * @return
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
    public List<Pair<Guid, Integer>> score(VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        boolean countThreadsAsCores = cluster.getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcEvenDistributionScore(vds, vm, countThreadsAsCores)));
        }
        return scores;
    }

}
