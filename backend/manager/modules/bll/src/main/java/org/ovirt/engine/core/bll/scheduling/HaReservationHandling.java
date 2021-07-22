package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.pending.PendingOvercommitMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for the scheduling mechanism for checking the HA Reservation status of a Cluster
 */
public class HaReservationHandling {

    private static final Logger log = LoggerFactory.getLogger(HaReservationHandling.class);

    private final PendingResourceManager pendingResourceManager;

    public HaReservationHandling(PendingResourceManager pendingResourceManager) {
        this.pendingResourceManager = pendingResourceManager;
    }

    /**
     * @param cluster
     *            - Cluster to check
     * @param failedHosts
     *            - a list to return all the hosts that failed the check, must be initialized outside this method
     * @return true: Cluster is HaReservation safe. false: a failover in one of the Clusters Hosts could negatively
     *         impacting performance.
     */
    public boolean checkHaReservationStatusForCluster(Cluster cluster, List<VDS> failedHosts) {
        List<VDS> hosts = Injector.get(VdsDao.class).getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);

        // No hosts, return true
        if (hosts == null || hosts.isEmpty()) {
            return true;
        }
        // HA Reservation is not possible with less than 2 hosts
        if (hosts.size() < 2) {
            log.debug("Cluster '{}' failed HA reservation check because there is only one host in the cluster",
                    cluster.getName());
            failedHosts.addAll(hosts);
            return false;
        }

        // List of host id and cpu/ram free resources
        // for the outer Pair, first is host id second is a Pair of cpu and ram
        // for the inner Pair, first is cpu second is ram
        List<Pair<Guid, Pair<Integer, Integer>>> hostsUnutilizedResources = getUnutilizedResources(hosts);

        Map<Guid, List<VM>> hostToHaVmsMapping = mapHaVmToHostByCluster(cluster.getId());

        for (VDS host : hosts) {
            if (hostToHaVmsMapping.get(host.getId()) != null) {
                boolean isHaSafe =
                        findReplacementForHost(cluster, host,
                                hostToHaVmsMapping.get(host.getId()),
                                hostsUnutilizedResources);
                if (!isHaSafe) {
                    failedHosts.add(host);
                }
            }
        }

        log.info("HA reservation status for cluster '{}' is '{}'",
                cluster.getName(),
                failedHosts.isEmpty() ? "OK" : "Failed");
        return failedHosts.isEmpty();
    }

    private boolean findReplacementForHost(Cluster cluster, VDS host,
            List<VM> vmList,
            List<Pair<Guid, Pair<Integer, Integer>>> hostsUnutilizedResources) {

        Map<Guid, Pair<Integer, Integer>> additionalHostsUtilizedResources = new HashMap<>();

        for (VM vm : vmList) {
            int curVmMemSize = 0;
            if(vm.getUsageMemPercent() != null) {
                curVmMemSize = (int) Math.round(vm.getMemSizeMb() * (vm.getUsageMemPercent() / 100.0));
            }

            // Make sure we reserve at least the guaranteed amount of memory or more
            // if the VM is using more than that.
            curVmMemSize = Math.max(curVmMemSize, vm.getMinAllocatedMem());

            int curVmCpuPercent = 0;
            if (vm.getUsageCpuPercent() != null) {
                curVmCpuPercent =
                        vm.getUsageCpuPercent() * VmCpuCountHelper.getDynamicNumOfCpu(vm)
                                / SlaValidator.getEffectiveCpuCores(host, cluster.getCountThreadsAsCores());
            }
            log.debug("VM '{}'. CPU usage: {}%, RAM required: {}MB", vm.getName(), curVmCpuPercent, curVmMemSize);

            boolean foundForCurVm = false;
            for (Pair<Guid, Pair<Integer, Integer>> hostData : hostsUnutilizedResources) {
                // Make sure not to run on the same Host as the Host we are testing
                if (hostData.getFirst().equals(host.getId())) {
                    continue;
                }

                // Check Memory and CPU
                if (hostData.getSecond() != null && hostData.getSecond().getSecond() != null
                        && hostData.getSecond().getFirst() != null) {

                    int memoryFree = hostData.getSecond().getSecond();
                    int cpuFree = hostData.getSecond().getFirst();

                    long additionalMemory = 0;
                    int additionalCpu = 0;

                    if (additionalHostsUtilizedResources.get(hostData.getFirst()) != null) {
                        additionalCpu = additionalHostsUtilizedResources.get(hostData.getFirst()).getFirst();
                        additionalMemory = additionalHostsUtilizedResources.get(hostData.getFirst()).getSecond();
                    }

                    if ((memoryFree - additionalMemory) >= curVmMemSize && (cpuFree - additionalCpu) >= curVmCpuPercent) {
                        // Found a place for current vm, add the RAM and CPU size to additionalHostsUtilizedResources
                        Pair<Integer, Integer> cpuRamPair = additionalHostsUtilizedResources.get(hostData.getFirst());
                        if (cpuRamPair != null) {
                            cpuRamPair.setFirst(cpuRamPair.getFirst() + curVmCpuPercent);
                            cpuRamPair.setSecond(cpuRamPair.getSecond() + curVmMemSize);
                        } else {
                            cpuRamPair = new Pair<>(curVmCpuPercent, curVmMemSize);
                            additionalHostsUtilizedResources.put(hostData.getFirst(), cpuRamPair);
                        }

                        foundForCurVm = true;
                        break;
                    }

                }

            }

            if (!foundForCurVm) {
                log.info("Did not found a replacement host for VM '{}'", vm.getName());
                return false;
            }

        }

        return true;
    }

    public static Map<Guid, List<VM>> mapVmToHost(List<VM> vms) {
        Map<Guid, List<VM>> hostToHaVmsMapping = new HashMap<>();

        for (VM vm : vms) {
            if (!Guid.isNullOrEmpty(vm.getRunOnVds())) {
                if (!hostToHaVmsMapping.containsKey(vm.getRunOnVds())) {
                    List<VM> vmsOfHost = new ArrayList<>();
                    vmsOfHost.add(vm);
                    hostToHaVmsMapping.put(vm.getRunOnVds(), vmsOfHost);
                } else {
                    hostToHaVmsMapping.get(vm.getRunOnVds()).add(vm);
                }
            }
        }
        return hostToHaVmsMapping;
    }

    private List<Pair<Guid, Pair<Integer, Integer>>> getUnutilizedResources(List<VDS> hosts) {
        List<Pair<Guid, Pair<Integer, Integer>>> hostsUnutilizedResources =
                new ArrayList<>();
        for (VDS host : hosts) {
            Pair<Integer, Integer> innerUnutilizedCpuRamPair = new Pair<>();
            int hostFreeCpu = 0;
            if (host.getUsageCpuPercent() != null) {
                hostFreeCpu = 100 - host.getUsageCpuPercent();
            }
            innerUnutilizedCpuRamPair.setFirst(hostFreeCpu);

            // Get available memory for the Host, round down to int
            int hostFreeMem = (int) host.getMaxSchedulingMemory()
                    - PendingOvercommitMemory.collectForHost(pendingResourceManager, host.getId());
            innerUnutilizedCpuRamPair.setSecond(hostFreeMem);

            Pair<Guid, Pair<Integer, Integer>> outerUnutilizedCpuRamPair =
                    new Pair<>(host.getId(), innerUnutilizedCpuRamPair);

            hostsUnutilizedResources.add(outerUnutilizedCpuRamPair);
        }
        return hostsUnutilizedResources;
    }

    public static Map<Guid, List<VM>> mapHaVmToHostByCluster(Guid clusterId) {

        List<VM> vms = Injector.get(VmDao.class).getAllForCluster(clusterId);
        if (vms == null || vms.isEmpty()) {
            log.debug("No VMs available for this cluster with id '{}'", clusterId);
            // return empty map
            return Collections.emptyMap();
        }

        vms = vms.stream().filter(VM::isAutoStartup).collect(Collectors.toList());
        return mapVmToHost(vms);
    }
}
