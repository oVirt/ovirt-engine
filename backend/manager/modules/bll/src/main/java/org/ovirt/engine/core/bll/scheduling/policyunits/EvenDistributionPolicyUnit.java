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

public class EvenDistributionPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionPolicyUnit(PolicyUnit policyUnit) {
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
        int vcpu = Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption);
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
            // round up result, fractions matters
            score = Math.min((int) Math.ceil(calcDistributeMetric(vds, vm, effectiveCpuCores)), MaxSchedulerWeight);
        }
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


    @Override
    public Pair<List<Guid>, Guid> balance(VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        List<VDS> relevantHosts = getVdsDao().getAllForVdsGroupWithoutMigrating(cluster.getId());
        if (relevantHosts == null || relevantHosts.size() < 2) {
            int hostCount = relevantHosts == null ? 0 : relevantHosts.size();
            log.debugFormat("No balancing for cluster {0}, contains only {1} host(s)", cluster.getName(), hostCount);
            return null;
        }
        // get vds that over committed for the time defined
        List<VDS> overUtilizedHosts = getOverUtilizedHosts(relevantHosts, parameters);

        if (overUtilizedHosts == null || overUtilizedHosts.size() == 0) {
            return null;
        }
        List<VDS> underUtilizedHosts = getUnderUtilizedHosts(cluster, relevantHosts, parameters);
        if (underUtilizedHosts == null || underUtilizedHosts.size() == 0) {
            return null;
        }
        VDS randomHost = overUtilizedHosts.get(new Random().nextInt(overUtilizedHosts.size()));
        List<VM> migrableVmsOnRandomHost = getMigrableVmsRunningOnVds(randomHost.getId());
        if(migrableVmsOnRandomHost.isEmpty()) {
            return null;
        }
        VM vm = getBestVmToMigrate(randomHost.getId(), migrableVmsOnRandomHost);

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
        }
        log.infoFormat("VdsLoadBalancer: vm selection - selected vm: {0}, cpu: {1}.", result.getName(),
                result.getUsageCpuPercent());
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
        final int highUtilization = tryParseWithDefault(parameters.get("HighUtilization"),
                getHighUtilizationDefaultValue());
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get("CpuOverCommitDurationMinutes"), Config
                        .<Integer> GetValue(ConfigValues.CpuOverCommitDurationMinutes));
        List<VDS> overUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getUsageCpuPercent() >= highUtilization
                        && p.getCpuOverCommitTimestamp() != null
                        && (new Date().getTime() - p.getCpuOverCommitTimestamp().getTime())
                        >= cpuOverCommitDurationMinutes * 1000L * 60L;
            }
        });
        Collections.sort(overUtilizedHosts, new ReverseComparator(new VdsCpuUsageComparator()));
        return overUtilizedHosts;
    }

    protected int getHighUtilizationDefaultValue() {
        return Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute);
    }

    protected List<VDS> getUnderUtilizedHosts(VDSGroup cluster,
            List<VDS> relevantHosts,
            Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get("HighUtilization"), Config
                .<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute));
        final int highVdsCount = Math
                .min(Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent)
                        * highUtilization / 100,
                        highUtilization
                                - Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage));
        List<VDS> underUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return (p.getUsageCpuPercent() + CalcSpmCpuConsumption(p)) < highVdsCount;
            }
        });
        Collections.sort(underUtilizedHosts, new VdsCpuUsageComparator());
        return underUtilizedHosts;
    }

    protected int CalcSpmCpuConsumption(VDS vds) {
        return ((vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption)
                * Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage) / vds.getCpuCores());
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
    protected final class VdsCpuUsageComparator implements Comparator<VDS> {
        @Override
        public int compare(VDS o1, VDS o2) {
            return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
        }

        private int calculateCpuUsage(VDS o1) {
            return o1.getUsageCpuPercent() * SlaValidator.getEffectiveCpuCores(o1) / o1.getVdsStrength();
        }
    }

    /**
     * Comparator that compares the CPU usage of two VMs, with regard to the number of CPUs each VM has.
     */
    private final class VmCpuUsageComparator implements Comparator<VM> {
        @Override
        public int compare(VM o1, VM o2) {
            return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
        }

        private int calculateCpuUsage(VM o1) {
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
}
