package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.DefaultMapper;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsCpuVdsLoadBalancingAlgorithm extends VdsLoadBalancingAlgorithm {

    public VdsCpuVdsLoadBalancingAlgorithm(VDSGroup group) {
        super(group);
    }

    @Override
    protected void InitOverUtilizedList() {
        // get vds that overcommited for the time defined

        List<VDS> relevantVdss = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getusage_cpu_percent() >= p.gethigh_utilization()
                        && p.getcpu_over_commit_time_stamp() != null
                        && (new Date().getTime() - p.getcpu_over_commit_time_stamp().getTime()) >= p
                                .getcpu_over_commit_duration_minutes() * 1000 * 60;
            }
        });

        Collections.sort(relevantVdss, createDescendingCpuComparator());

        setOverUtilizedServers(LinqUtils.toMap(relevantVdss, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getId();
            }
        }));

        log.infoFormat("VdsLoadBalancer: number of over utilized vdss found: {0}.", getOverUtilizedServers().size());
    }

    @SuppressWarnings("unchecked")
    private Comparator<VDS> createDescendingCpuComparator() {
        return new ReverseComparator(new VdsCpuUsageComparator());
    }

    @Override
    protected void InitReadyToMigrationList() {
        final int highVdsCount = Math
                .min(Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent)
                        * getVdsGroup().gethigh_utilization() / 100,
                        getVdsGroup().gethigh_utilization()
                                - Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage));

        final boolean isEvenlyDistribute = VdsSelectionAlgorithm.EvenlyDistribute == getVdsGroup().getselection_algorithm();
        List<VDS> relevantVdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return (p.getusage_cpu_percent() + CalcSpmCpuConsumption(p)) < highVdsCount
                        && (isEvenlyDistribute || p.getusage_cpu_percent() > p.getlow_utilization());
            }
        });
        Collections.sort(relevantVdses, new VdsCpuUsageComparator());

        setReadyToMigrationServers(LinqUtils.toMap(relevantVdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS i) {
                return i.getId();
            }
        }));

        log.infoFormat("VdsLoadBalancer: max cpu limit: {0}, number of ready to migration vdss: {1}", highVdsCount,
                getReadyToMigrationServers().size());
    }

    private int CalcSpmCpuConsumption(VDS vds) {
        return ((vds.getspm_status() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption)
                * Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage) / vds.getcpu_cores());
    }

    @Override
    protected void InitUnderUtilizedList() {
        // get vds that undercommited for the time defined, order first by
        // vm_count and then vds strength
        if (VdsSelectionAlgorithm.EvenlyDistribute != getVdsGroup().getselection_algorithm()) {
            List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
                @Override
                public boolean eval(VDS p) {
                    return p.getusage_cpu_percent() <= p.getlow_utilization()
                            && p.getcpu_over_commit_time_stamp() != null
                            && (new Date().getTime() - p.getcpu_over_commit_time_stamp().getTime()) >= p
                                    .getcpu_over_commit_duration_minutes() * 60 * 1000;
                }
            });
            // The order of sorting will be from smallest to biggest. The vm will be
            // moved from less underutilized host to more underutilized host
            Collections.sort(vdses, new Comparator<VDS>() {
                @Override
                public int compare(VDS o1, VDS o2) {
                    int primary = o1.getvm_count() - o2.getvm_count();
                    if (primary != 0)
                        return primary;
                    else {
                        return new VdsCpuUsageComparator().compare(o1, o2);
                    }
                }
            });

            setUnderUtilizedServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
                @Override
                public Guid createKey(VDS vds) {
                    return vds.getId();
                }
            }));

            log.infoFormat("VdsLoadBalancer: number of under utilized hosts found: {0}.",
                    getUnderUtilizedServers().size());
        } else {
            setUnderUtilizedServers(Collections.EMPTY_MAP);
        }
    }

    @Override
    protected VM getBestVmToMigrate(List<VM> vms, final Guid vdsId) {

        // get the vm with the min cpu usage that its not his dedicated vds
        List<VM> vms1 = LinqUtils.filter(vms, new Predicate<VM>() {
            @Override
            public boolean eval(VM v) {
                return !vdsId.equals(v.getDedicatedVmForVds());
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
        log.infoFormat("VdsLoadBalancer: vm selection - selected vm: {0}, cpu: {1}.", result.getVmName(),
                result.getUsageCpuPercent());
        return result;
    }

    private static Log log = LogFactory.getLog(VdsCpuVdsLoadBalancingAlgorithm.class);

    /**
     * Comparator that compares the CPU usage of two hosts, with regard to the number of CPUs each host has and it's
     * strength.
     */
    private final class VdsCpuUsageComparator implements Comparator<VDS> {
        @Override
        public int compare(VDS o1, VDS o2) {
            return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
        }

        private int calculateCpuUsage(VDS o1) {
            return o1.getusage_cpu_percent() * o1.getcpu_cores() / o1.getvds_strength();
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
}
