package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class VirtMonitoringStrategyTest {
    private Guid vdsId = Guid.newGuid();
    private Guid vdsId2 = Guid.newGuid();
    private Guid vdsIdNoImageTransfers = Guid.newGuid();
    private Guid vdsIdNoActiveImageTransfers = Guid.newGuid();
    private Guid vdsIdImageTransfersRunning = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();
    private VDS vdsFromDb;
    private Cluster cluster;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaintenanceVdsIgnoreExternalVms, true),
                MockConfigDescriptor.of(ConfigValues.ClusterEmulatedMachines, Version.v4_4,
                        Arrays.asList("pc-i440fx-rhel7.6.0", "pc-q35-rhel9.6.0")),
                MockConfigDescriptor.of(ConfigValues.ClusterEmulatedMachines, Version.v4_8,
                        Arrays.asList("pc-i440fx-rhel10.0.0", "pc-q35-rhel10.0.0")));
    }

    @BeforeEach
    public void setUp() {
        vdsFromDb = new VDS();
        vdsFromDb.setId(vdsId);
        vdsFromDb.setClusterId(clusterId);

        clusterDao = mockCluster();
        virtStrategy = spy(new VirtMonitoringStrategy(clusterDao, mockVdsDao(), mockVmDao(), null, mockImageTransferDao()));
        doNothing().when(virtStrategy).vdsNonOperational(any(), any(), any());
    }

    private VirtMonitoringStrategy virtStrategy;
    private ClusterDao clusterDao;

    @Test
    public void testVirtCanMoveToMaintenance() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.PreparingForMaintenance);
        vds.setId(vdsId);
        vds.setClusterId(clusterId);
        assertFalse(virtStrategy.canMoveToMaintenance(vds));
        vds.setId(vdsId2);
        doReturn(false).when(virtStrategy).isAnyNonExternalVmRunningOnVds(vds);
        assertTrue(virtStrategy.canMoveToMaintenance(vds));
        doReturn(true).when(virtStrategy).isAnyNonExternalVmRunningOnVds(vds);
        assertFalse(virtStrategy.canMoveToMaintenance(vds));
        // next, testing hosts with/without image transfers so first make sure there's no running vm on the host
        doReturn(false).when(virtStrategy).isAnyNonExternalVmRunningOnVds(vds);
        vds.setId(vdsIdNoImageTransfers);
        assertTrue(virtStrategy.canMoveToMaintenance(vds));
        vds.setId(vdsIdNoActiveImageTransfers);
        assertTrue(virtStrategy.canMoveToMaintenance(vds));
        vds.setId(vdsIdImageTransfersRunning);
        assertFalse(virtStrategy.canMoveToMaintenance(vds));
    }

    @Test
    public void testVirtIsMonitoringNeeded() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.NonOperational);
        assertTrue(virtStrategy.isMonitoringNeeded(vds));
        vds.setStatus(VDSStatus.Up);
        assertTrue(virtStrategy.isMonitoringNeeded(vds));
    }

    @Test
    public void testProcessSpecialSoftwareCapabilities() {
        VDS vds = createBaseVds();
        vds.setHostOs("Fedora - 20 - 3");
        vds.setId(vdsId);
        vds.setClusterId(clusterId);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus());
        vds.setKvmEnabled(Boolean.TRUE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus());
        vds.setKvmEnabled(Boolean.FALSE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus());
        vds.setKvmEnabled(Boolean.TRUE);
        vds.setStatus(VDSStatus.Up);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus());
        vds.getSupportedRngSources().clear();
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus());
    }

    @Test
    public void testProtectRhel7InRhel6() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        vds.setHostOs("RHEL - 7Server - 1.el7");
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus());
    }

    @Test
    public void testProtectRhel6InRhel7() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 7Server - 1.el7");
        vds.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus());
    }

    @Test
    public void testAllowRhel7InRhel6() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        vds.setHostOs("RHEL - 7Server - 1.el7");
        cluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        virtStrategy.processSoftwareCapabilities(vds);
        assertNotEquals(VDSStatus.NonOperational, vds.getStatus());
    }

    @Test
    public void testAllowRhel6InRhel7() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 7Server - 1.el7");
        vds.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        cluster.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        virtStrategy.processSoftwareCapabilities(vds);
        assertNotEquals(VDSStatus.NonOperational, vds.getStatus());
    }

    // --- Host compliance with cluster emulation mode ---

    @Test
    public void testHostCompliesWithClusterEmulationModeQ35OnlyClusterEl10Host() {
        // EL10 host in cluster with Q35 as default should stay Operational
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-q35-rhel9.6.0,pc-q35-rhel10.0.0,pc-i440fx-rhel10.0.0");
        cluster.setEmulatedMachine("pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus(), "EL10 host should stay Operational in cluster with Q35 default");
    }

    @Test
    public void testHostCompliesWithClusterEmulationModeMissingQ35() {
        // Host missing Q35 support should go Non-Operational
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-i440fx-rhel7.6.0");
        cluster.setEmulatedMachine("pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus(), "Host missing Q35 support should go Non-Operational");
    }

    @Test
    public void testHostCompliesWithClusterEmulationModeEl9Host() {
        // EL9 host in cluster with Q35 as default stays Operational
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        cluster.setEmulatedMachine("pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus(), "EL9 host should stay Operational in cluster with Q35 default");
    }

    @Test
    public void testHostCompliesWithClusterEmulationModeNullClusterEmulatedMachine() {
        // cluster.emulated_machine is nullable (the upgrade migration can set it to NULL
        // together with detect_emulated_machine=TRUE). With the detection flag off and a
        // null default — an inconsistent state — the host must be treated as non-compliant
        // instead of throwing an NPE.
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-q35-rhel9.6.0");
        cluster.setEmulatedMachine(null);
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus(),
                "Host must be Non-Operational when the cluster default emulated machine is null");
    }

    @Test
    public void testHostCompliesWithLegacyDualFormatClusterEmulatedMachine() {
        // Legacy combined "i440fx;q35" cluster default (pre-migration): a host supporting the
        // Q35 part should stay Operational
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.Up, vds.getStatus(), "Host supporting Q35 part of legacy combined cluster machine should stay Operational");
    }

    @Test
    public void testHostCompliesWithLegacyDualFormatClusterEmulatedMachineNoMatch() {
        // Legacy combined cluster default where the host supports neither part
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-q35-rhel8.0.0");
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(false);
        virtStrategy.processSoftwareCapabilities(vds);
        assertEquals(VDSStatus.NonOperational, vds.getStatus(), "Host supporting neither part of legacy combined cluster machine should go Non-Operational");
    }

    @Test
    public void testDetectModeBelow48WritesDualDefault() {
        // Below 4.8 the dual format is the supported cluster default: the first compliant
        // host must write both parts, not a single Q35 value.
        VDS vds = createBaseVds();
        vds.setSupportedEmulatedMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        cluster.setDetectEmulatedMachine(true);
        virtStrategy.processSoftwareCapabilities(vds);

        assertEquals(VDSStatus.Up, vds.getStatus(),
                "A host supporting both parts must comply in detect mode");
        verify(clusterDao).setEmulatedMachine(eq(clusterId),
                eq("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0"), anyBoolean());
    }

    @Test
    public void testDetectModeAt48WritesSingleQ35() {
        // At level >= 4.8 the cluster default is Q35-only: detect mode writes the single
        // Q35 match even when the host also supports i440fx machine types.
        VDS vds = createBaseVds();
        vds.setClusterCompatibilityVersion(Version.v4_8);
        vds.setSupportedEmulatedMachines("pc-i440fx-rhel10.0.0,pc-q35-rhel10.0.0");
        cluster.setDetectEmulatedMachine(true);
        virtStrategy.processSoftwareCapabilities(vds);

        assertEquals(VDSStatus.Up, vds.getStatus(),
                "A host supporting Q35 must comply in detect mode");
        verify(clusterDao).setEmulatedMachine(eq(clusterId),
                eq("pc-q35-rhel10.0.0"), anyBoolean());
    }

    private VDS createBaseVds() {
        VDS vds = new VDS();
        vds.setClusterCompatibilityVersion(Version.v4_4);
        vds.setSupportedEmulatedMachines("pc-1.0");
        vds.getSupportedRngSources().add(VmRngDevice.Source.URANDOM);
        vds.setStatus(VDSStatus.Up);
        vds.setId(vdsId);
        vds.setClusterId(clusterId);
        return vds;
    }

    @Test
    public void testNeedToProcessHardwareCapsFalse() {
        VDS oldVds = new VDS();
        oldVds.setClusterId(Guid.newGuid());
        oldVds.setId(Guid.newGuid());
        oldVds.setCpuFlags("flag1");
        VDS newVds = oldVds.clone();
        assertFalse(virtStrategy.processHardwareCapabilitiesNeeded(oldVds, newVds));
    }

    @Test
    public void testNeedToProcessHardwareCapsTrue() {
        VDS oldVds = new VDS();
        oldVds.setClusterId(Guid.newGuid());
        oldVds.setId(Guid.newGuid());
        oldVds.setCpuFlags("flag1");
        VDS newVds = oldVds.clone();
        newVds.setCpuFlags("flag2");
        assertTrue(virtStrategy.processHardwareCapabilitiesNeeded(oldVds, newVds));
    }

    private ImageTransferDao mockImageTransferDao() {
        ImageTransferDao mock = mock(ImageTransferDao.class);
        ImageTransfer imageTransferFinished = new ImageTransfer();
        ImageTransfer imageTransferWip = new ImageTransfer();
        imageTransferFinished.setPhase(ImageTransferPhase.FINISHED_SUCCESS);
        when(mock.getByVdsId(vdsIdNoImageTransfers)).thenReturn(Collections.emptyList());
        when(mock.getByVdsId(vdsIdNoActiveImageTransfers)).thenReturn(Arrays.asList(imageTransferFinished));
        imageTransferWip.setPhase(ImageTransferPhase.TRANSFERRING);
        when(mock.getByVdsId(vdsIdImageTransfersRunning)).thenReturn(Arrays.asList(imageTransferWip));
        return mock;
    }

    private ClusterDao mockCluster() {
        ClusterDao mock = mock(ClusterDao.class);
        cluster = new Cluster();
        cluster.setEmulatedMachine("pc-1.0");
        cluster.setCompatibilityVersion(Version.getLast());
        cluster.setClusterId(clusterId);
        when(mock.get(clusterId)).thenReturn(cluster);
        return mock;
    }

    private VdsDao mockVdsDao() {
        VdsDao mock = mock(VdsDao.class);
        when(mock.getFirstUpRhelForCluster(clusterId)).thenReturn(vdsFromDb);
        return mock;
    }

    private VmDao mockVmDao() {
        VmDao mock = mock(VmDao.class);
        VM vm = mock(VM.class);
        when(vm.isExternalVm()).thenReturn(Boolean.FALSE);
        when(mock.getAllRunningForVds(vdsId)).thenReturn(Collections.singletonList(vm));

        VM externalVm = mock(VM.class);
        when(externalVm.isExternalVm()).thenReturn(Boolean.TRUE);
        when(mock.getAllRunningForVds(vdsId2)).thenReturn(Collections.singletonList(externalVm));
        return mock;
    }

}
