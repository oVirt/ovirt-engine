package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.snapshots.CountMacUsageDifference;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;

@Singleton
public class MacsUsedAcrossWholeSystem {

    @Inject
    private VmDao vmDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private SnapshotsManager snapshotsManager;

    /**
     * mac initialization should read from snapshots and add all the macs of the stateless snapshots (of running vms)
     * that differ from the macs of same vnics in the current vm config or the vnic doesn't exist in the current vm
     * config.
     *
     * @param macPoolId pool being initialized
     *
     * @return all MACs which should be registered in MAC pool.
     */
    public List<String> getMacsForMacPool(Guid macPoolId) {
        List<Guid> idsOfAllClustersHavingMacPool = getIdsOfAllClustersHavingMacPool(macPoolId);

        Map<Guid, VM> vmsById = idsOfAllClustersHavingMacPool.stream()
                .flatMap(clusterId -> vmDao.getAllForCluster(clusterId).stream())
                .collect(Collectors.toMap(VM::getId, Function.identity()));

        Stream<Guid> idsOfRunningStatelessVMs = vmsById.values()
                .stream()
                .filter(VM::isRunning)
                .filter(VM::isStateless)
                .map(VM::getId);

        Stream<VM> statelessSnapshotsOfRunningVMs =
                idsOfRunningStatelessVMs.map(snapshotsManager::getVmConfigurationInStatelessSnapshotOfVm)
                        .filter(Optional::isPresent)
                        .map(Optional::get);

        Map<Guid, List<VmNetworkInterface>> snapshottedInterfacesByVmId =
                statelessSnapshotsOfRunningVMs.collect(Collectors.toMap(VM::getId, VM::getInterfaces));

        List<String> macsToBeAllocated = vmsById.keySet()
                .stream()
                .flatMap(vmId -> calculateAllMacsUsedInVmAndItsSnapshot(vmsById.get(vmId).getInterfaces(),
                        snapshottedInterfacesByVmId.get(vmId)))
                .collect(Collectors.toList());

        return macsToBeAllocated;
    }

    private Stream<String> calculateAllMacsUsedInVmAndItsSnapshot(List<VmNetworkInterface> vmInterfaces,
            List<VmNetworkInterface> snapshotInterfaces) {

        CountMacUsageDifference countMacUsageDifference =
                new CountMacUsageDifference(macAddressesOfInterfaces(snapshotInterfaces),
                        macAddressesOfInterfaces(vmInterfaces));

        Stream<String> macsDuplicatedByNumberOfTimesTheyAreUsed = countMacUsageDifference.maxUsage().entrySet()
                .stream()
                .flatMap(entry -> LongStream.range(0, entry.getValue()).boxed().map(e -> entry.getKey()));

        return macsDuplicatedByNumberOfTimesTheyAreUsed;
    }

    private Stream<String> macAddressesOfInterfaces(List<VmNetworkInterface> interfaces) {
        if (interfaces == null) {
            return Stream.empty();
        }
        return interfaces.stream().map(VmNetworkInterface::getMacAddress);
    }

    private List<Guid> getIdsOfAllClustersHavingMacPool(Guid macPoolId) {
        return clusterDao.getAllClustersByMacPoolId(macPoolId)
                .stream()
                .distinct()
                .map(Cluster::getId)
                .collect(Collectors.toList());
    }
}
