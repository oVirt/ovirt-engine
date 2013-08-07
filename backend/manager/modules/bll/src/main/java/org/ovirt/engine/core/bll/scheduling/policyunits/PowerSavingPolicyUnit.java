package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class PowerSavingPolicyUnit extends EvenDistributionPolicyUnit {

    public PowerSavingPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<Pair<Guid, Integer>> score(List<VDS> hosts, VM vm, Map<String, String> parameters) {
        List<Pair<Guid, Integer>> scores = new ArrayList<Pair<Guid, Integer>>();
        for (VDS vds : hosts) {
            scores.add(new Pair<Guid, Integer>(vds.getId(), 100 - vds.getUsageCpuPercent()));
        }
        return scores;
    }

    @Override
    public Pair<List<Guid>, Guid> balance(VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        return super.balance(cluster, hosts, parameters, messages);
    }

    @Override
    protected List<VDS> getOverUtilizedHosts(List<VDS> relevantHosts,
            Map<String, String> parameters) {
        final int lowUtilization = tryParseWithDefault(parameters.get("LowUtilization"), Config
                .<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave));
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get("CpuOverCommitDurationMinutes"), Config
                        .<Integer> GetValue(ConfigValues.CpuOverCommitDurationMinutes));
        List<VDS> overUtilized = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getUsageCpuPercent() <= lowUtilization
                        && p.getCpuOverCommitTimestamp() != null
                        && (new Date().getTime() - p.getCpuOverCommitTimestamp().getTime()) >=
                        cpuOverCommitDurationMinutes * 60L * 1000L;
            }
        });
        // The order of sorting will be from smallest to biggest. The vm will be
        // moved from less underutilized host to more underutilized host
        Collections.sort(overUtilized, new Comparator<VDS>() {
            @Override
            public int compare(VDS o1, VDS o2) {
                int primary = o1.getVmCount() - o2.getVmCount();
                if (primary != 0)
                    return primary;
                else {
                    return new VdsCpuUsageComparator().compare(o1, o2);
                }
            }
        });
        overUtilized.addAll(super.getOverUtilizedHosts(relevantHosts, parameters));
        return overUtilized;
    }

    @Override
    protected int getHighUtilizationDefaultValue() {
        return Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave);
    }

    @Override
    protected List<VDS> getUnderUtilizedHosts(VDSGroup cluster,
            List<VDS> relevantHosts,
            Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get("HighUtilization"), Config
                .<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave));
        final int lowUtilization = tryParseWithDefault(parameters.get("LowUtilization"), Config
                .<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave));
        final int highVdsCount = Math
                .min(Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent)
                        * highUtilization / 100,
                        highUtilization
                                - Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage));
        List<VDS> underUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return (p.getUsageCpuPercent() + CalcSpmCpuConsumption(p)) < highVdsCount
                        && (p.getUsageCpuPercent() > lowUtilization);
            }
        });
        Collections.sort(underUtilizedHosts, new VdsCpuUsageComparator());
        return underUtilizedHosts;
    }
}
