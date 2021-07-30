package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "736999d0-1023-46a4-9a75-1316ed50e15b",
        name = "OptimalForCpuPowerSaving",
        description = "Gives hosts with higher CPU usage, lower weight (means that hosts with higher CPU usage are"
                + " more likely to be selected)",
        type = PolicyUnitType.WEIGHT,
        parameters = PolicyUnitParameter.HIGH_UTILIZATION
)
public class PowerSavingCPUWeightPolicyUnit extends EvenDistributionCPUWeightPolicyUnit {

    public PowerSavingCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        long highUtilization = context.getPolicyParameters().containsKey(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()) ?
                Long.parseLong(context.getPolicyParameters().get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()))
                : Long.MAX_VALUE;

        boolean countThreadsAsCores = context.getCluster().getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        List<Guid> hostsWithMaxScore = new ArrayList<>();
        for (VDS vds : hosts) {
            Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
            if (effectiveCpuCores == null || vds.getUsageCpuPercent() == null) {
                hostsWithMaxScore.add(vds.getId());
                continue;
            }

            // If the host is overutilized, return the worst score
            int hostLoad = calcHostLoad(vds, effectiveCpuCores);
            if (hostLoad > highUtilization * effectiveCpuCores) {
                hostsWithMaxScore.add(vds.getId());
                continue;
            }

            // Using negative score
            int score = -(int)Math.round(calcHostLoadPerCore(vds, vmGroup, effectiveCpuCores, hostLoad, 0));
            scores.add(new Pair<>(vds.getId(), score));
        }

        stretchScores(scores);
        scores.addAll(hostsWithMaxScore.stream()
                .map(id -> new Pair<>(id, getMaxSchedulerWeight()))
                .collect(Collectors.toList()));

        return scores;
    }
}
