package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

public class VirtMonitoringStrategyTest {

    private VDS vdsFromDb = new VDS();

    public VirtMonitoringStrategyTest() {
        virtStrategy = spy(new VirtMonitoringStrategy(mockVdsGroup(), mockVdsDao()));
        doNothing().when(virtStrategy).vdsNonOperational(any(VDS.class), any(NonOperationalReason.class), any(Map.class));
    }

    private VirtMonitoringStrategy virtStrategy;

    @Test
    public void testVirtCanMoveToMaintenance() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.PreparingForMaintenance);
        vds.setVmCount(1);
        assertFalse(virtStrategy.canMoveToMaintenance(vds));
        vds.setVmCount(0);
        assertTrue(virtStrategy.canMoveToMaintenance(vds));
    }

    @Test
    public void testVirtIsMonitoringNeeded() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.NonOperational);
        vds.setVmCount(1);
        assertTrue(virtStrategy.isMonitoringNeeded(vds));
        vds.setVmCount(0);
        assertFalse(virtStrategy.isMonitoringNeeded(vds));
        vds.setStatus(VDSStatus.Up);
        assertTrue(virtStrategy.isMonitoringNeeded(vds));
    }

    @Test
    public void testProcessSpecialSoftwareCapabilities() {
        VDS vds = createBaseVds();
        vds.setHostOs("Fedora - 20 - 3");
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.Up));
        vds.setKvmEnabled(Boolean.TRUE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.Up));
        vds.setKvmEnabled(Boolean.FALSE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.NonOperational));
        vds.setKvmEnabled(Boolean.TRUE);
        vds.setStatus(VDSStatus.Up);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.Up));
        vds.getSupportedRngSources().clear();
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.NonOperational));
    }

    @Test
    public void testProtectRhel7InRhel6() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        vds.setHostOs("RHEL - 7Server - 1.el7");
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.NonOperational));
    }

    @Test
    public void testProtectRhel6InRhel7() {
        VDS vds = createBaseVds();
        vdsFromDb.setHostOs("RHEL - 7Server - 1.el7");
        vds.setHostOs("RHEL - 6Server - 6.5.0.1.el6");
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.NonOperational));
    }

    private VDS createBaseVds() {
        VDS vds = new VDS();
        vds.setSupportedEmulatedMachines("pc-1.0");
        vds.getSupportedRngSources().add(VmRngDevice.Source.RANDOM);
        vds.setStatus(VDSStatus.Up);
        return vds;
    }

    @Test
    public void testNeedToProcessHardwareCapsFalse() {
        VDS oldVds = new VDS();
        oldVds.setVdsGroupId(Guid.newGuid());
        oldVds.setId(Guid.newGuid());
        oldVds.setCpuFlags("flag1");
        VDS newVds = oldVds.clone();
        assertFalse(virtStrategy.processHardwareCapabilitiesNeeded(oldVds, newVds));
    }

    @Test
    public void testNeedToProcessHardwareCapsTrue() {
        VDS oldVds = new VDS();
        oldVds.setVdsGroupId(Guid.newGuid());
        oldVds.setId(Guid.newGuid());
        oldVds.setCpuFlags("flag1");
        VDS newVds = oldVds.clone();
        newVds.setCpuFlags("flag2");
        assertTrue(virtStrategy.processHardwareCapabilitiesNeeded(oldVds, newVds));
    }

    private VdsGroupDAO mockVdsGroup() {
        VdsGroupDAO mock = mock(VdsGroupDAO.class);
        VDSGroup value = new VDSGroup();
        value.setEmulatedMachine("pc-1.0");
        value.getRequiredRngSources().add(VmRngDevice.Source.RANDOM);
        org.mockito.Mockito.when(mock.get(any(Guid.class))).thenReturn(value);
        return mock;
    }

    private VdsDAO mockVdsDao() {
        VdsDAO mock = mock(VdsDAO.class);
        when(mock.getFirstUpRhelForVdsGroup(any(Guid.class))).thenReturn(vdsFromDb);
        return mock;
    }
}
