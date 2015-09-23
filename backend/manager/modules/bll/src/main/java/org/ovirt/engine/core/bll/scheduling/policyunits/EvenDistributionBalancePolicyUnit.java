package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.FindVmAndDestinations;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class EvenDistributionBalancePolicyUnit extends CpuAndMemoryBalancingPolicyUnit {

    private static final String HIGH_UTILIZATION = "HighUtilization";

    public EvenDistributionBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    protected int getHighUtilizationDefaultValue() {
        return Config.<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute);
    }

    protected FindVmAndDestinations getFindVmAndDestinations(VDSGroup cluster, Map<String, String> parameters) {
        final int highCpuUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        final long overUtilizedMemory = parameters.containsKey(EvenDistributionBalancePolicyUnit.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED) ?
                Long.parseLong(parameters.get(EvenDistributionBalancePolicyUnit.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED)) : 0L;

        return new FindVmAndDestinations(cluster, highCpuUtilization, overUtilizedMemory);
    }

    @Override
    protected List<VDS> getPrimarySources(VDSGroup cluster,
                                          List<VDS> candidateHosts,
                                          Map<String, String> parameters) {
        final int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get("CpuOverCommitDurationMinutes"), Config
                        .<Integer> getValue(ConfigValues.CpuOverCommitDurationMinutes));

        return getOverUtilizedCPUHosts(candidateHosts, highUtilization, cpuOverCommitDurationMinutes);
    }

    @Override
    protected List<VDS> getPrimaryDestinations(VDSGroup cluster,
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
                tryParseWithDefault(parameters.get("CpuOverCommitDurationMinutes"), Config
                        .<Integer> getValue(ConfigValues.CpuOverCommitDurationMinutes));

        return getUnderUtilizedCPUHosts(candidateHosts, lowUtilization, 0, cpuOverCommitDurationMinutes);
    }

    @Override
    protected List<VDS> getSecondarySources(VDSGroup cluster,
                                            List<VDS> candidateHosts,
                                            Map<String, String> parameters) {
        long requiredMemory = parameters.containsKey(LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED) ?
                Long.parseLong(parameters.get(LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED)) : 0L;

        return getOverUtilizedMemoryHosts(candidateHosts, requiredMemory);
    }

    @Override
    protected List<VDS> getSecondaryDestinations(VDSGroup cluster,
                                                 List<VDS> candidateHosts,
                                                 Map<String, String> parameters) {
        long requiredMemory = parameters.containsKey(HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED) ?
                Long.parseLong(parameters.get(HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED)) : 0L;

        return getUnderUtilizedMemoryHosts(candidateHosts, requiredMemory, 0);
    }

}
