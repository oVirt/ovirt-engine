package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class, ExecutorServiceExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class MacsUsedAcrossWholeSystemTest {

/*
 * Macs in db has following structure:
 *
 * • Cluster1: has vm, which has 1 MAC on it's one Nic. Mac is used just once.
 * • Cluster2: has stateless vm, which has 1 snapshot. Both has NIC, which has same MAC. Mac is used just once.
 * • Cluster3: has stateless vm, which has 1 snapshot. Both has NIC, which is different in VM and snapshot. Each
 *             mac is used once.
 * • Cluster4: has stateless vm, which has 1 snapshot. Nic from snapshot was updated, so that its mac was changed,
 *             and its mac is used in different mac. Mac from snapshot is used twice, because it's not linked to same
 *             nic in VM, other nic is used once.
 * • Cluster5: contains two VMs, which snapshot should be ignored.
 *
 * • Cluster6: contains VMs, used for testing, that preexisting duplicity among VMs or snapshots has to be introduced
 *             into pool.
 *
 *
 *                       .----------.     .-----.   .------.     .------.
 *      .--------------->| Cluster1 |---->| Vm1 |-->| Nic1 |---->| Mac1 |
 *      |                '----------'     '-----'   '------'     '------'
 * .---------.           .----------.     .-----.                  .-----------.
 * | MacPool |---------->| Cluster2 |---->| Vm2 |----------------->| Snapshot2 |
 * '---------'           '----------'     '-----'                  '-----------'
 *      |                                    |                           |
 *      |                                    v                           v
 *      |                                .------.   .------.   .-------------------.
 *      |                                | nic2 |-->| Mac2 |<--| nic2_fromSnapshot |
 *      |                                '------'   '------'   '-------------------'
 *      |                .----------.    .-----.              .-----------.
 *      |--------------->| Cluster3 |--->| Vm3 |------------->| Snapshot3 |
 *      |                '----------'    '-----'              '-----------'
 *      |                                   |                       |
 *      |                                   v                       v
 *      |                               .------.          .-------------------.
 *      |                               | nic3 |          | nic3_fromSnapshot |
 *      |                               '------'          '-------------------'
 *      |                                   |                       |
 *      |                                   v                       v
 *      |                               .-------.               .-------.
 *      |                               | mac31 |               | mac32 |
 *      |                               '-------'               '-------'
 *      |                .----------.    .-----.                .-----------.
 *      |--------------->| Cluster4 |--->| Vm4 |--------------->| Snapshot4 |
 *      |                '----------'    '-----'                '-----------'
 *      |                                   |                         |
 *      |                                   |                         v
 *      |                      .-------.    |    .-------. .--------------------.
 *      |                      | nic41 |<---'--->| nic42 | | nic41_fromSnapshot |
 *      |                      '-------'         '-------' '--------------------'
 *      |                          |                 |                |
 *      |                          v                 |                |
 *      |                      .-------.             |   .-------.    |
 *      |                      | mac42 |             '-->| mac41 |<---'
 *      |                      '-------'                 '-------'
 *      |                 .----------.     .--------------.    .-----------.
 *      |---------------->| Cluster5 |---->| NotRunningVm |--->| Snapshot5 |
 *      |                 '----------'     '--------------'    '-----------'
 *      |                       |                  |                 |
 *      |                       |                  v                 v
 *      |                       |              .------.    .-------------------.
 *      |                       |              | Nic5 |    | nic5_fromSnapshot |
 *      |                       |              '------'    '-------------------'
 *      |                       |                  |                 |
 *      |                       |                  v                 v
 *      |                       |              .-------.         .-------.
 *      |                       |              | Mac51 |         | Mac52 |
 *      |                       |              '-------'         '-------'
 *      |                       |           .----------------.    .-----------.
 *      |                       '---------->| NotStatelessVm |--->| Snapshot6 |
 *      |                                   '----------------'    '-----------'
 *      |                                            |                  |
 *      |                                            v                  v
 *      |                                        .------.     .-------------------.
 *      |                                        | Nic6 |     | nic6_fromSnapshot |
 *      |                                        '------'     '-------------------'
 *      |                                            |                  |
 *      |                                            v                  v
 *      |                                        .-------.          .-------.
 *      |                                        | Mac61 |          | Mac62 |
 *      |                                        '-------'          '-------'
 *      |                  .----------.    .------------------.
 *      '----------------->| Cluster6 |--->| VmWithDuplicates |
 *                         '----------'    '------------------'
 *                               |       .-------.   |   .-------.
 *                               |       | Nic71 |<--'-->| nic72 |
 *                               |       '-------'       '-------'
 *                               |           |               |
 *                               |           v               v
 *                               |       .------.        .------.
 *                               |       | Mac7 |        | Mac7 |
 *                               |       '------'        '------'
 *                               |         .--------------------------------.      .-----------.
 *                               '-------->| VmHavingSnapshotWithDuplicates |----->| Snapshot7 |
 *                                         '--------------------------------'      '-----------'
 *                                                          |                .-------.   |   .-------.
 *                                                          v                | Nic81 |<--'-->| nic82 |
 *                                                      .-------.            '-------'       '-------'
 *                                                      | Nic83 |                |               |
 *                                                      '-------'                v               v
 *                                                          |                .------.        .------.
 *                                                          v                | Mac8 |        | Mac8 |
 *                                                      .------.             '------'        '------'
 *                                                      | Mac8 |
 *                                                      '------'
 */


    @Mock
    private VmDao vmDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private SnapshotsManager snapshotsManager;

    @InjectMocks
    private MacsUsedAcrossWholeSystem underTest;

    @Test
    public void testGetMacsForMacPool() {
        MacPool macPool = createMacPool();

        String mac1 = "mac1";
        String mac2 = "mac2";
        String mac31 = "mac31";
        String mac32 = "mac32";
        String mac41 = "mac41";
        String mac42 = "mac42";
        String mac51 = "mac51";
        String mac52 = "mac52";
        String mac61 = "mac61";
        String mac62 = "mac62";
        String mac7 = "mac7";
        String mac8 = "mac8";

        Cluster cluster1 = createCluster(macPool);
        VmNetworkInterface nic1 = createNic(mac1);
        VM vm1 = createVm(cluster1, nic1);

        Cluster cluster2 = createCluster(macPool);
        VmNetworkInterface nic2 = createNic(mac2);
        VmNetworkInterface nic2_fromSnapshot = createNic(mac2, nic2.getId());
        VM vm2 = createVm(cluster2, nic2);
        VM snapshot2 = createSnapshot(cluster2, vm2, nic2_fromSnapshot);

        Cluster cluster3 = createCluster(macPool);
        Guid nic3Id = Guid.newGuid();
        VmNetworkInterface nic3 = createNic(mac31, nic3Id);
        VM vm3 = createVm(cluster3, nic3);
        VmNetworkInterface nic3_fromSnapshot = createNic(mac32, nic3Id);
        VM snapshot3 = createSnapshot(cluster3, vm3, nic3_fromSnapshot);

        Cluster cluster4 = createCluster(macPool);
        VmNetworkInterface nic41 = createNic(mac42);
        VmNetworkInterface nic41_fromSnapshot = createNic(mac41);
        VmNetworkInterface nic42 = createNic(mac41);
        VM vm4 = createVm(cluster4, nic41, nic42);
        VM snapshot4 = createSnapshot(cluster4, vm4, nic41_fromSnapshot);

        Cluster cluster5 = createCluster(macPool);
        VmNetworkInterface nic5 = createNic(mac51);
        VmNetworkInterface nic5_fromSnapshot = createNic(mac52);
        VM notRunningVm = createVm(cluster5, Guid.newGuid(), false, true, nic5);
        VM snapshot5 = createSnapshot(cluster5, notRunningVm, nic5_fromSnapshot);

        VmNetworkInterface nic6 = createNic(mac61);
        VmNetworkInterface nic6_fromSnapshot = createNic(mac62);
        VM notStatelessVm = createVm(cluster5, Guid.newGuid(), true, false, nic6);
        VM snapshot6 = createSnapshot(cluster5, notStatelessVm, nic6_fromSnapshot);


        Cluster cluster6 = createCluster(macPool);
        VmNetworkInterface nic71 = createNic(mac7);
        VmNetworkInterface nic72 = createNic(mac7);
        VM vmWithDuplicates = createVm(cluster6, nic71, nic72);

        VmNetworkInterface nic81 = createNic(mac8);
        VmNetworkInterface nic82 = createNic(mac8);
        VmNetworkInterface nic83 = createNic(mac8);
        VM vmHavingSnapshotWithDuplicates = createVm(cluster6, nic83);
        VM snapshot7 = createSnapshot(cluster6, vmHavingSnapshotWithDuplicates, nic81, nic82);

        //mocking:
        when(clusterDao.getAllClustersByMacPoolId(macPool.getId()))
                .thenReturn(Arrays.asList(cluster1, cluster2, cluster3, cluster4, cluster5, cluster6));

        mockClusterVms(cluster1, vm1);
        mockClusterVms(cluster2, vm2);
        mockClusterVms(cluster3, vm3);
        mockClusterVms(cluster4, vm4);
        mockClusterVms(cluster5, notRunningVm, notStatelessVm);
        mockClusterVms(cluster6, vmWithDuplicates, vmHavingSnapshotWithDuplicates);

        mockVmSnapshots(vm1, Optional.empty());
        mockVmSnapshots(vm2, Optional.of(snapshot2));
        mockVmSnapshots(vm3, Optional.of(snapshot3));
        mockVmSnapshots(vm4, Optional.of(snapshot4));
        mockVmSnapshots(notRunningVm, Optional.of(snapshot5));
        mockVmSnapshots(notStatelessVm, Optional.of(snapshot6));
        mockVmSnapshots(vmHavingSnapshotWithDuplicates, Optional.of(snapshot7));

        //verifying
        List<String> macsForMacPool = underTest.getMacsForMacPool(macPool.getId());
        assertThat(macsForMacPool, Matchers.containsInAnyOrder(
                mac1,
                mac2,
                mac31,
                mac32,
                mac41,
                mac42,
                mac51,
                mac61,
                mac7,
                mac7,
                mac8,
                mac8
        ));
    }

    private void mockVmSnapshots(VM vm1, Optional<VM> snapshot) {
        when(snapshotsManager.getVmConfigurationInStatelessSnapshotOfVm(vm1.getId())).thenReturn(snapshot);
    }

    private void mockClusterVms(Cluster cluster1, VM... vms) {
        when(vmDao.getAllForCluster(cluster1.getId())).thenReturn(Arrays.asList(vms));
    }

    private VM createSnapshot(Cluster cluster, VM vm, VmNetworkInterface ... nic) {
        VM vm1 = new VM();

        vm1.setId(vm.getId());

        vm1.setInterfaces(new ArrayList<>(Arrays.asList(nic)));
        vm1.setClusterId(cluster.getId());

        return vm1;
    }

    private MacPool createMacPool() {
        MacPool result = new MacPool();

        result.setId(Guid.newGuid());

        return result;
    }

    private Cluster createCluster(MacPool macPool) {
        Cluster cluster = new Cluster();

        cluster.setId(Guid.newGuid());
        cluster.setMacPoolId(macPool.getId());

        return cluster;
    }

    private VmNetworkInterface createNic(String macAddress) {
        return createNic(macAddress, Guid.newGuid());
    }

    private VmNetworkInterface createNic(String macAddress, Guid id) {
        VmNetworkInterface nic = new VmNetworkInterface();

        nic.setId(id);
        nic.setMacAddress(macAddress);

        return nic;
    }

    private VM createVm(Cluster cluster, VmNetworkInterface... nic) {
        return createVm(cluster, Guid.newGuid(), nic);
    }

    private VM createVm(Cluster cluster, Guid vmId, VmNetworkInterface... nics) {
        return createVm(cluster, vmId, true, true, nics);
    }

    private VM createVm(Cluster cluster, Guid vmId, boolean running, boolean stateless, VmNetworkInterface... nics) {
        VM vm = new VM();

        vm.setId(vmId);

        vm.setInterfaces(new ArrayList<>(Arrays.asList(nics)));
        vm.setClusterId(cluster.getId());
        vm.setStatus(running ? VMStatus.Up : VMStatus.Down);
        vm.setStateless(stateless);

        when(vmNicDao.<VmNic>getAllForVm(vm.getId())).<VmNic>thenReturn(new ArrayList<>(vm.getInterfaces()));

        return vm;
    }

}
