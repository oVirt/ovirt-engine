package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
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

    public FindVmAndDestinations(Cluster cluster, int highCpuUtilization, long requiredMemory) {
        this.cluster = cluster;
        this.highCpuUtilization = highCpuUtilization;
        this.requiredMemory = requiredMemory;
    }

    public List<BalanceResult> invoke(List<VDS> sourceHosts,
            List<VDS> destinationHosts,
            VmDao vmDao,
            ResourceManager resourceManager) {

        Map<Guid, List<VM>> vmsForHost =  getMigratableVmsRunningOnHosts(vmDao,
                sourceHosts.stream().map(VDS::getId).collect(Collectors.toList()));

        List<BalanceResult> results = new ArrayList<>();
        for (VDS sourceHost: sourceHosts) {
            List<VM> migratableVms = vmsForHost.get(sourceHost.getId());

            // Statistics are needed for sorting by cpu usage
            migratableVms.forEach(vm -> vm.setStatisticsData(resourceManager.getVmManager(vm.getId(), false).getStatistics()));
            migratableVms.sort(VmCpuUsageComparator.INSTANCE);

            for (VM vm : migratableVms){
                // Check if vm not over utilize memory or CPU of destination hosts
                List<VDS> validDestinationHosts = getValidHosts(
                        destinationHosts, cluster, vm, highCpuUtilization, requiredMemory);

                if (!validDestinationHosts.isEmpty()){
                    // Add the current host, it is possible it is the best host after all,
                    // because the balancer does not know about affinity for example
                    validDestinationHosts.add(sourceHost);

                    results.add(new BalanceResult(
                            vm.getId(),
                            validDestinationHosts.stream()
                                    .map(VDS::getId)
                                    .collect(Collectors.toList())
                    ));
                }
            }
        }
        return results;
    }

    /**
     * Return all VMs that run on a hosts and can be migrated away.
     *
     * This method is to be considered private. It is protected to be available
     * from unit tests.
     *
     * @param vmDao The data source to get the VM information
     * @param hostIds Ids of hosts the returned VMs run at
     * @return Map of host ID to VMs that run on host and can be migrated
     */
    private static Map<Guid, List<VM>> getMigratableVmsRunningOnHosts(final VmDao vmDao, final Collection<Guid> hostIds) {
        Map<Guid, List<VM>> vmsForHost = vmDao.getAllRunningForMultipleVds(hostIds);
        vmsForHost.forEach((id, vms) ->
                vms.removeIf(vm -> vm.getMigrationSupport() != MigrationSupport.MIGRATABLE)
        );

        return vmsForHost;
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
            if (vds.getId().equals(vm.getRunOnVds())) {
                continue;
            }

            // Using host threads, so the predicted VM cpu is consistent
            // with the percentage that vdsm returns
            int predictedVmCpu = (vm.getStatisticsData() != null && vm.getUsageCpuPercent() != null && vds.getCpuThreads() != null) ?
                    (vm.getUsageCpuPercent() * VmCpuCountHelper.getDynamicNumOfCpu(vm) / vds.getCpuThreads()):
                    0;

            if (vds.getUsageCpuPercent() + predictedVmCpu <= highCpuUtilization
                    && vds.getMaxSchedulingMemory() - vm.getMemSizeMb() > minimalFreeMemory) {
                result.add(vds);
            }
        }

        return result;
    }
}
