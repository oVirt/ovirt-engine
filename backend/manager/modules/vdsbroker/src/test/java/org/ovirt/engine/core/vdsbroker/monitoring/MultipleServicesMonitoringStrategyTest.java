package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class MultipleServicesMonitoringStrategyTest {
    private Guid vdsId = Guid.newGuid();
    private Guid vdsId2 = Guid.newGuid();
    VirtMonitoringStrategy virtStrategy;
    GlusterMonitoringStrategy glusterStrategy;
    MultipleServicesMonitoringStrategy strategy;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.MaintenanceVdsIgnoreExternalVms, true));
    }

    public MultipleServicesMonitoringStrategyTest() {
        virtStrategy =
                spy(new VirtMonitoringStrategy(mock(ClusterDao.class), mock(VdsDao.class), mockVmDao(), null));
        doReturn(false).when(virtStrategy).isAnyVmRunOnVdsInDb(any());
        glusterStrategy = spy(new GlusterMonitoringStrategy());
        doNothing().when(virtStrategy).vdsNonOperational(any(), any(), any());
        strategy = spy(new MultipleServicesMonitoringStrategy());
        strategy.addMonitoringStrategy(virtStrategy);
        strategy.addMonitoringStrategy(glusterStrategy);
    }

    @Test
    public void testCanMoveVdsToMaintenanceFalse() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.PreparingForMaintenance);
        vds.setId(vdsId);
        assertFalse(strategy.canMoveToMaintenance(vds));
    }

    @Test
    public void testCanMoveVdsToMaintenanceTrue() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.PreparingForMaintenance);
        vds.setId(vdsId2);
        assertTrue(strategy.canMoveToMaintenance(vds));
    }

    @Test
    public void testIsmonitoringNeededTrue() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.NonOperational);
        vds.setVmCount(1);
        assertTrue(strategy.isMonitoringNeeded(vds));
        vds.setStatus(VDSStatus.Up);
        assertTrue(strategy.isMonitoringNeeded(vds));
    }

    @Test
    public void testIsmonitoringNeededGlusterTrue() {
        VDS vds = new VDS();
        vds.setStatus(VDSStatus.NonOperational);
        vds.setVmCount(0);
        assertTrue(strategy.isMonitoringNeeded(vds));
    }

    @Test
    public void testProcessingSoftwareGluster() {
        doThrow(new RuntimeException()).when(glusterStrategy).processSoftwareCapabilities(any());
        VDS vds = new VDS();
        assertThrows(RuntimeException.class, () -> strategy.processSoftwareCapabilities(vds));
    }

    @Test
    public void testProcessingHardwareVirt() {
        doThrow(new RuntimeException()).when(virtStrategy).processHardwareCapabilities(any());
        VDS vds = new VDS();
        assertThrows(RuntimeException.class, () -> strategy.processHardwareCapabilities(vds));
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
