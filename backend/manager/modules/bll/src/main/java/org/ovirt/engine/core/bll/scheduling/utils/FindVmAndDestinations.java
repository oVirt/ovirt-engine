package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find a VM that can be migrated and a set of hosts that can potentially
 * receive it while not violating the utilization rules.
 */
public class FindVmAndDestinations {
    private static final Logger log = LoggerFactory.getLogger(FindVmAndDestinations.class);

    private Cluster cluster;
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

    public FindVmAndDestinations(Cluster cluster, int highCpuUtilization, long requiredMemory) {
        this.cluster = cluster;
        this.highCpuUtilization = highCpuUtilization;
        this.requiredMemory = requiredMemory;
    }

    public Result invoke(List<VDS> sourceHosts, List<VDS> destinationHosts, VmDao vmDao) {
        List<VDS> validDestinationHosts;
        // Iterate over source hosts until you find valid vm to migrate, hosts sorted by cpu usage
        for (VDS sourceHost : sourceHosts){
            // Get list of all migratable vms on host
            List<VM> migratableVmsOnHost = getMigratableVmsRunningOnVds(vmDao, sourceHost.getId());
            if (migratableVmsOnHost != null && !migratableVmsOnHost.isEmpty()){
                // Sort vms by cpu usage
                Collections.sort(migratableVmsOnHost, VmCpuUsageComparator.INSTANCE);
                for (VM vmToMigrate : migratableVmsOnHost){
                    if (vmToMigrate != null){
                        // Check if vm not over utilize memory or CPU of destination hosts
                        validDestinationHosts = getValidHosts(
                                destinationHosts, cluster, vmToMigrate, highCpuUtilization, requiredMemory
                        );
                        if (!validDestinationHosts.isEmpty()){
                            log.debug("Vm '{}' selected for migration", vmToMigrate.getName());
                            return new Result(vmToMigrate, validDestinationHosts);
                        }
                    }
                }
            }
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
     * This method is to be considered private. It is protected to be available
     * from unit tests.
     *
     * @param vmDao The data source to get the VM information
     * @param hostId Id of a host the returned VMs run at
     * @return list od VM that run on host and can be migrated
     */
    protected List<VM> getMigratableVmsRunningOnVds(final VmDao vmDao, final Guid hostId) {
        List<VM> vmsFromDB = vmDao.getAllRunningForVds(hostId);

        return vmsFromDB.stream()
                .filter(
                        vm -> vm.getMigrationSupport() == MigrationSupport.MIGRATABLE
                ).collect(Collectors.toList());
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
    private List<VDS> getValidHosts(Collection<VDS> candidates, Cluster cluster, VM vm, int highCpuUtilization, long minimalFreeMemory) {
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
