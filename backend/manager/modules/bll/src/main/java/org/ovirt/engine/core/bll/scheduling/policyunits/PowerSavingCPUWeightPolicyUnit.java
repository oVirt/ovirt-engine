package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
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
        guid = "736999d0-1023-46a4-9a75-1316ed50e15b",
        name = "OptimalForCpuPowerSaving",
        description = "Gives hosts with higher CPU usage, lower weight (means that hosts with higher CPU usage are"
                + " more likely to be selected)",
        type = PolicyUnitType.WEIGHT,
        parameters = {
                PolicyUnitParameter.HIGH_UTILIZATION
        }
)
public class PowerSavingCPUWeightPolicyUnit extends EvenDistributionCPUWeightPolicyUnit {

    private long highUtilization;

    public PowerSavingCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        highUtilization = parameters.containsKey(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()))
                : Long.MAX_VALUE;

        return super.score(cluster, hosts, vm, parameters);
    }

    @Override
    protected int calcHostScore(VDS vds, VM vm, boolean countThreadsAsCores) {
        // If the host is overutilized, return the worst score
        if (vds.getUsageCpuPercent() == null || vds.getUsageCpuPercent() > highUtilization) {
            return getMaxSchedulerWeight() - 1;
        }

        return (getMaxSchedulerWeight() - 1) - super.calcHostScore(vds, vm, countThreadsAsCores);
    }
}
