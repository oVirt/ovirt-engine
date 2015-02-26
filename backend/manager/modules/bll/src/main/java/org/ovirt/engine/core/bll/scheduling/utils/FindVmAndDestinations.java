package org.ovirt.engine.core.bll.scheduling.utils;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Find a VM that can be migrated and a set of hosts that can potentially
 * receive it while not violating the utilization rules.
 */
public class FindVmAndDestinations {
    private static final Logger log = LoggerFactory.getLogger(FindVmAndDestinations.class);

    private VDSGroup cluster;
    private int highCpuUtilization;
    private long requiredMemory;

    public static class Result {
        private List<VDS> destinationHosts;
        private VM vmToMigrate;

        public Result(VM vmToMigrate, List<VDS> destinationHosts) {
            this.vmToMigrate = vmToMigrate;
            this.destinationHosts = destinationHosts;
        }

        public List<VDS> getDestinationHosts() {
            return destinationHosts;
        }

        public VM getVmToMigrate() {
            return vmToMigrate;
        }
    }

    public FindVmAndDestinations(VDSGroup cluster, int highCpuUtilization, long requiredMemory) {
        this.cluster = cluster;
        this.highCpuUtilization = highCpuUtilization;
        this.requiredMemory = requiredMemory;
    }

    public Result invoke(List<VDS> sourceHosts, List<VDS> destinationHosts, VmDAO vmDAO) {
        VDS randomHost = sourceHosts.get(new Random().nextInt(sourceHosts.size()));
        List<VM> migrableVmsOnRandomHost = getMigratableVmsRunningOnVds(vmDAO, randomHost.getId());

        VM vmToMigrate = getBestVmToMigrate(migrableVmsOnRandomHost);

        if (vmToMigrate != null
                && !migrableVmsOnRandomHost.isEmpty()) {
            return new Result(
                    vmToMigrate,

                    // check that underutilized host's CPU + predicted VM cpu is less than threshold,
                    // to prevent the VM to be bounced between hosts.
                    getValidHosts(destinationHosts, cluster, vmToMigrate, highCpuUtilization, requiredMemory)
            );
        }

        return null;
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

    /**
     * Return all VMs that run on a host and can be migrated away.
     *
     * @param vmDAO The data source to get the VM information
     * @param hostId Id of a host the returned VMs run at
     * @return list od VM that run on host and can be migrated
     */
    private List<VM> getMigratableVmsRunningOnVds(final VmDAO vmDAO, final Guid hostId) {
        List<VM> vmsFromDB = vmDAO.getAllRunningForVds(hostId);

        List<VM> vms = LinqUtils.filter(vmsFromDB, new Predicate<VM>() {
            @Override
            public boolean eval(VM v) {
                // The VM has to allow migrations and...
                return v.getMigrationSupport() == MigrationSupport.MIGRATABLE
                        // must not be pinned to the host
                        && !hostId.equals(v.getDedicatedVmForVds());
            }
        });

        return vms;
    }

    /**
     * Return the best VM for migration. The default implementation
     * returns the VM with the lowest CPU usage.
     *
     * @param vms candidate VMs
     * @return the "best" VM
     */
    protected VM getBestVmToMigrate(final List<VM> vms) {
        VM result = null;

        if (!vms.isEmpty()) {
            result = Collections.min(vms, VmCpuUsageComparator.INSTANCE);
        }

        // if no vm found return the vm with min cpu
        if (result == null) {
            log.info("VdsLoadBalancer: vm selection - no vm without pending found.");
            result = Collections.min(vms, VmCpuUsageComparator.INSTANCE);
        } else {
            log.info("VdsLoadBalancer: vm selection - selected vm: '{}', cpu: {}.", result.getName(),
                    result.getUsageCpuPercent());
        }
        return result;
    }

    /**
     * Pre-filter candidate hosts with regards to the balancing policy. The default
     * implementation does not allow putting VM on a host that would become
     * over-utilized to prevent bouncing.
     *
     * @param candidates candidate hosts
     * @param cluster cluster reference
     * @param vm vm to be migrated
     * @param highCpuUtilization CPU over-utilization threshold in percents
     * @param minimalFreeMemory Memory over-utilization threshold in MB
     * @return list of hosts that satisfy the balancing contraints
     */
    private List<VDS> getValidHosts(Collection<VDS> candidates, VDSGroup cluster, VM vm, int highCpuUtilization, long minimalFreeMemory) {
        List<VDS> result = new ArrayList<>();

        for (VDS vds: candidates) {
            int predictedVmCpu = getPredictedVmCpu(vm, vds, cluster.getCountThreadsAsCores());
            if (vds.getUsageCpuPercent() + predictedVmCpu <= highCpuUtilization
                    && vds.getMaxSchedulingMemory() - vm.getMemSizeMb() > minimalFreeMemory) {
                result.add(vds);
            }
        }

        return result;
    }
}
