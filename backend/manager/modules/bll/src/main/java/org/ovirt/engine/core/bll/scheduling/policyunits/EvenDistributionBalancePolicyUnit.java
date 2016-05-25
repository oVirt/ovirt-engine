package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.FindVmAndDestinations;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483ed2",
        name = "OptimalForEvenDistribution",
        type = PolicyUnitType.LOAD_BALANCING,
        description = "Load balancing VMs in cluster according to hosts CPU load, striving cluster's hosts CPU load to"
                + " be under 'HighUtilization'",
        parameters = {
                PolicyUnitParameter.HIGH_UTILIZATION,
                PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED,
                PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED,
                PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES
        }
)
public class EvenDistributionBalancePolicyUnit extends CpuAndMemoryBalancingPolicyUnit {

    private static final String HIGH_UTILIZATION = PolicyUnitParameter.HIGH_UTILIZATION.getDbName();

    public EvenDistributionBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    protected int getHighUtilizationDefaultValue() {
        return Config.<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute);
    }

    @Override
    protected FindVmAndDestinations getFindVmAndDestinations(Cluster cluster, Map<String, String> parameters) {
        final int highCpuUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        final long overUtilizedMemory = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;

        return new FindVmAndDestinations(cluster, highCpuUtilization, overUtilizedMemory);
    }

    @Override
    protected List<VDS> getPrimarySources(Cluster cluster,
                                          List<VDS> candidateHosts,
                                          Map<String, String> parameters) {
        final int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()),
                        Config.<Integer> getValue(ConfigValues.CpuOverCommitDurationMinutes));

        return getOverUtilizedCPUHosts(candidateHosts, highUtilization, cpuOverCommitDurationMinutes);
    }

    @Override
    protected List<VDS> getPrimaryDestinations(Cluster cluster,
                                               List<VDS> candidateHosts,
                                               Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION), Config
                .<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute));
        final int lowUtilization = Math
                .min(Config.<Integer> getValue(ConfigValues.UtilizationThresholdInPercent)
                                * highUtilization / 100,
                        highUtilization
                                - Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage));
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()),
                        Config.<Integer> getValue(ConfigValues.CpuOverCommitDurationMinutes));

        return getUnderUtilizedCPUHosts(candidateHosts, lowUtilization, 0, cpuOverCommitDurationMinutes);
    }

    @Override
    protected List<VDS> getSecondarySources(Cluster cluster,
                                            List<VDS> candidateHosts,
                                            Map<String, String> parameters) {
        long requiredMemory = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;

        return getOverUtilizedMemoryHosts(candidateHosts, requiredMemory);
    }

    @Override
    protected List<VDS> getSecondaryDestinations(Cluster cluster,
                                                 List<VDS> candidateHosts,
                                                 Map<String, String> parameters) {
        long requiredMemory = parameters.containsKey(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName())) : 0L;

        return getUnderUtilizedMemoryHosts(candidateHosts, requiredMemory, 0);
    }

}
