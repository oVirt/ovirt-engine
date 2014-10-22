package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
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
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvenDistributionBalancePolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(EvenDistributionBalancePolicyUnit.class);

    private static final String HIGH_UTILIZATION = "HighUtilization";

    public EvenDistributionBalancePolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public Pair<List<Guid>, Guid> balance(final VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        if (hosts == null || hosts.size() < 2) {
            int hostCount = hosts == null ? 0 : hosts.size();
            log.debug("No balancing for cluster '{}', contains only {} host(s)", cluster.getName(), hostCount);
            return null;
        }
        // get vds that over committed for the time defined
        /* returns list of Hosts with
         *    cpuUtilization >= highUtilization
         *    && cpuOverCommitMinutes >= CpuOverCommitDurationMinutes
         */
        List<VDS> overUtilizedHosts = getOverUtilizedHosts(hosts, parameters);

        // if there aren't any overutilized hosts, then there is nothing to balance...
        if (overUtilizedHosts == null || overUtilizedHosts.size() == 0) {
            log.debug("There is no over-utilized host in cluster '{}'", cluster.getName());
            return null;
        }

        // returns hosts with utilization lower than the specified threshold
        List<VDS> underUtilizedHosts = getUnderUtilizedHosts(cluster, hosts, parameters);

        //if no host has a spare power, then there is nothing we can do to balance it..
        if (underUtilizedHosts == null || underUtilizedHosts.size() == 0) {
            log.warn("All hosts are over-utilized, can't balance the cluster '{}'", cluster.getName());
            return null;
        }
        VDS randomHost = overUtilizedHosts.get(new Random().nextInt(overUtilizedHosts.size()));
        List<VM> migrableVmsOnRandomHost = getMigrableVmsRunningOnVds(randomHost.getId());
        if(migrableVmsOnRandomHost.isEmpty()) {
            return null;
        }
        final VM vm = getBestVmToMigrate(randomHost.getId(), migrableVmsOnRandomHost);

        // check that underutilized host's CPU + predicted VM cpu is less than threshold,
        // to prevent the VM to be bounced between hosts.
        final int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        underUtilizedHosts = LinqUtils.filter(underUtilizedHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS vds) {
                int predictedVmCpu = getPredictedVmCpu(vm, vds, cluster.getCountThreadsAsCores());
                return vds.getUsageCpuPercent() + predictedVmCpu <= highUtilization;
            }
        });
        if (underUtilizedHosts.size() == 0) {
            return null;
        }

        List<Guid> underUtilizedHostsKeys = new ArrayList<Guid>();
        for (VDS vds : underUtilizedHosts) {
            underUtilizedHostsKeys.add(vds.getId());
        }

        return new Pair<List<Guid>, Guid>(underUtilizedHostsKeys, vm.getId());
    }

    protected VM getBestVmToMigrate(final Guid hostId, List<VM> vms) {
        // get the vm with the min cpu usage that its not his dedicated vds
        List<VM> vms1 = LinqUtils.filter(vms, new Predicate<VM>() {
            @Override
            public boolean eval(VM v) {
                return !hostId.equals(v.getDedicatedVmForVds());
            }
        });
        VM result = null;
        if (!vms1.isEmpty()) {
            result = Collections.min(vms1, new VmCpuUsageComparator());
        }

        // if no vm found return the vm with min cpu
        if (result == null) {
            log.info("VdsLoadBalancer: vm selection - no vm without pending found.");
            result = Collections.min(vms, new VmCpuUsageComparator());
        } else {
            log.info("VdsLoadBalancer: vm selection - selected vm: '{}', cpu: {}.", result.getName(),
                    result.getUsageCpuPercent());
        }
        return result;
    }

    private List<VM> getMigrableVmsRunningOnVds(Guid hostId) {
        List<VM> vmsFromDB = getVmDao().getAllRunningForVds(hostId);

        List<VM> vms = LinqUtils.filter(vmsFromDB, new Predicate<VM>() {
            @Override
            public boolean eval(VM v) {
                return v.getMigrationSupport() == MigrationSupport.MIGRATABLE;
            }
        });

        return vms;
    }

    protected List<VDS> getOverUtilizedHosts(List<VDS> relevantHosts,
            Map<String, String> parameters) {
        final int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION),
                getHighUtilizationDefaultValue());
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get("CpuOverCommitDurationMinutes"), Config
                        .<Integer> getValue(ConfigValues.CpuOverCommitDurationMinutes));
        List<VDS> overUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getUsageCpuPercent() >= highUtilization
                        && p.getCpuOverCommitTimestamp() != null
                        && (new Date().getTime() - p.getCpuOverCommitTimestamp().getTime())
                        >= cpuOverCommitDurationMinutes * 1000L * 60L
                        && p.getVmCount() > 0;
            }
        });
        Collections.sort(overUtilizedHosts, new ReverseComparator(new VdsCpuUsageComparator()));
        return overUtilizedHosts;
    }

    protected int getHighUtilizationDefaultValue() {
        return Config.<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute);
    }

    protected List<VDS> getUnderUtilizedHosts(VDSGroup cluster,
            List<VDS> relevantHosts,
            Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get(HIGH_UTILIZATION), Config
                .<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute));
        final int highVdsCount = Math
                .min(Config.<Integer> getValue(ConfigValues.UtilizationThresholdInPercent)
                        * highUtilization / 100,
                        highUtilization
                                - Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage));
        List<VDS> underUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return (p.getUsageCpuPercent() + calcSpmCpuConsumption(p)) < highVdsCount;
            }
        });
        Collections.sort(underUtilizedHosts, new VdsCpuUsageComparator());
        return underUtilizedHosts;
    }

    protected int calcSpmCpuConsumption(VDS vds) {
        return ((vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> getValue(ConfigValues.SpmVCpuConsumption)
                * Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage) / vds.getCpuCores());
    }

    protected VdsDAO getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }
    /**
     * Comparator that compares the CPU usage of two hosts, with regard to the number of CPUs each host has and it's
     * strength.
     */
    protected static final class VdsCpuUsageComparator implements Comparator<VDS> {
        @Override
        public int compare(VDS o1, VDS o2) {
            return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
        }

        private static int calculateCpuUsage(VDS o1) {
            return o1.getUsageCpuPercent() * SlaValidator.getEffectiveCpuCores(o1) / o1.getVdsStrength();
        }
    }

    /**
     * Comparator that compares the CPU usage of two VMs, with regard to the number of CPUs each VM has.
     */
    private static final class VmCpuUsageComparator implements Comparator<VM> {
        @Override
        public int compare(VM o1, VM o2) {
            return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
        }

        private static int calculateCpuUsage(VM o1) {
            return o1.getUsageCpuPercent() * o1.getNumOfCpus();
        }
    }

    protected int tryParseWithDefault(String candidate, int defaultValue) {
        if (candidate != null) {
            try {
                return Integer.parseInt(candidate);
            } catch (Exception e) {
                // do nothing
            }
        }
        return defaultValue;
    }

    /**
     * The predicted CPU the CPU that the VM will take considering
     * how many cores it has and how many cores the host has.
     * @return
     *          predicted vm cpu
     */
    protected int getPredictedVmCpu(VM vm, VDS vds, boolean countThreadsAsCores) {
        Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
        if (vm.getUsageCpuPercent() != null && effectiveCpuCores != null) {
            return (vm.getUsageCpuPercent() * vm.getNumOfCpus())
                        / effectiveCpuCores;
        }
        return 0;
    }
}
