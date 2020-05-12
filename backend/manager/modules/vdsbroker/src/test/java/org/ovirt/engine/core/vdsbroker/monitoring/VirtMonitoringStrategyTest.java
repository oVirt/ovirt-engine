package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class VirtMonitoringStrategyTest {
    private Guid vdsId = Guid.newGuid();
    private Guid vdsId2 = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();

    private VDS vdsFromDb;
    private Cluster cluster;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.MaintenanceVdsIgnoreExternalVms, true));
    }

    @BeforeEach
    public void setUp() {
        vdsFromDb = new VDS();
        vdsFromDb.setId(vdsId);
        vdsFromDb.setClusterId(clusterId);

        virtStrategy = spy(new VirtMonitoringStrategy(mockCluster(), mockVdsDao(), mockVmDao(), null));
        doNothing().when(virtStrategy).vdsNonOperational(any(), any(), any());
    }

    private VirtMonitoringStrategy virtStrategy;

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
