package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class EvenDistributionWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionWeightPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
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
        double pendingVcpus = vds.getPendingVcpusCount();

        return (hostCpu / vcpu) + (pendingVcpus + vm.getNumOfCpus() + spmCpu) / hostCores;
    }

    protected int calcEvenDistributionScore(VDS vds, VM vm, boolean countThreadsAsCores) {
        int score = MaxSchedulerWeight - 1;
        Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
        if (effectiveCpuCores != null
                && vds.getUsageCpuPercent() != null
                && vds.getPendingVcpusCount() != null) {
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
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(hosts.get(0).getVdsGroupId());
        boolean countThreadsAsCores = vdsGroup != null ? vdsGroup.getCountThreadsAsCores() : false;
        List<Pair<Guid, Integer>> scores = new ArrayList<Pair<Guid, Integer>>();
        for (VDS vds : hosts) {
            scores.add(new Pair<Guid, Integer>(vds.getId(), calcEvenDistributionScore(vds, vm, countThreadsAsCores)));
        }
        return scores;
    }

}
