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

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483edb",
        name = "OptimalForCpuEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description = "Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more"
                + " likely to be selected)"
)
public class EvenDistributionCPUWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        boolean countThreadsAsCores = cluster.getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcHostScore(vds, vm, countThreadsAsCores)));
        }
        return scores;
    }

    private double calcHostLoadPerCore(VDS vds, VM vm, int hostCores) {
        int vcpu = Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer>getValue(ConfigValues.SpmVCpuConsumption);

        double hostCpu = vds.getUsageCpuPercent();
        double pendingVcpus = PendingCpuCores.collectForHost(getPendingResourceManager(), vds.getId());

        double hostLoad = hostCpu * hostCores;
        double addedLoad = vcpu * (pendingVcpus + vm.getNumOfCpus() + spmCpu);

        return (hostLoad + addedLoad) / hostCores;
    }

    /**
     * Calculate a single host weight score according to various parameters.
     *
     * @param vds                     host on which the score is calculated for
     * @param vm                      virtual machine to be deployed at a selected host by score
     * @param countThreadsAsCores     true - count threads as cores , false - otherwise
     * @return weight score for a single host
     */
    protected int calcHostScore(VDS vds, VM vm, boolean countThreadsAsCores) {
        Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
        if (effectiveCpuCores == null || vds.getUsageCpuPercent() == null) {
            return getMaxSchedulerWeight() - 1;
        }

        double loadPerCore = calcHostLoadPerCore(vds, vm, effectiveCpuCores);

        // Scale so that 110 %  maps to MaxSchedulerWeight - 2
        double score = loadPerCore * (getMaxSchedulerWeight() - 2) / 110.0;

        // rounding the result and adding one to avoid zero
        return Math.min((int) Math.round(score) + 1, getMaxSchedulerWeight() - 1);
    }
}
