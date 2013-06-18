package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsGroupDAO;

public class VirtMonitoringStrategyTest {

    public VirtMonitoringStrategyTest() {
        virtStrategy = spy(new VirtMonitoringStrategy(mockVdsGroup()));
        doNothing().when(virtStrategy).vdsNonOperational(any(VDS.class), any(NonOperationalReason.class),any(Map.class));
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
        VDS vds = new VDS();
        vds.setSupportedEmulatedMachines("pc-1.0");
        vds.setStatus(VDSStatus.Up);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.Up));
        vds.setKvmEnabled(Boolean.TRUE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.Up));
        vds.setKvmEnabled(Boolean.FALSE);
        virtStrategy.processSoftwareCapabilities(vds);
        assertTrue(vds.getStatus().equals(VDSStatus.NonOperational));
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
        org.mockito.Mockito.when(mock.get(any(Guid.class))).thenReturn(value);
        return mock;
    }
}
