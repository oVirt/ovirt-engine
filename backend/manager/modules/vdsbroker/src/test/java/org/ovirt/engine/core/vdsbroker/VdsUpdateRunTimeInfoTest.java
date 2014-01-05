package org.ovirt.engine.core.vdsbroker;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@RunWith(MockitoJUnitRunner.class)
public class VdsUpdateRunTimeInfoTest {

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    private VDS vds;
    HashMap[] vmInfo;
    List<VmDynamic> poweringUpVms;
    Map<Guid, VmInternalData> runningVms;

    VdsUpdateRunTimeInfo updater;

    @Mock
    VdsGroupDAO groupDAO;

    @Mock
    VmDAO vmDAO;

    @Mock
    DiskDao diskDAO;

    @Mock
    DbFacade dbFacade;

    @Mock
    VDSGroup cluster;

    @Mock
    VmDeviceDAO vmDeviceDAO;

    AuditLogDAO mockAuditLogDao = new AuditLogDaoMocker();

    @Before
    public void setup() {
        initVds();
        initConditions();
        updater = new VdsUpdateRunTimeInfo(null, vds, mock(MonitoringStrategy.class)) {

            @Override
            public DbFacade getDbFacade() {
                return dbFacade;
            }

            @Override
            protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
                AuditLog al = new AuditLog();
                al.setlog_type(logType);
                mockAuditLogDao.save(al);
            }

            @Override
            protected Map[] getVmInfo(List<String> vmsToUpdate) {
                return vmInfo;
            }

            @Override
            protected List<VmDynamic> getPoweringUpVms() {
                return poweringUpVms;
            }

            @Override
            protected Map<Guid, VmInternalData> getRunningVms() {
                return runningVms;
            }

        };
    }

    @Test
    public void updateVmDevicesNull() {
        updater.updateVmDevices(Collections.singletonList(""));

        assertEquals("wrong number of new devices", 0, updater.getNewVmDevices().size());
        assertEquals("wrong number of removed devices", 0, updater.getRemovedVmDevices().size());
    }

    @Test
    public void updateVmDevicesNotNull() {
        Guid vmGuid = Guid.newGuid();
        when(vmDeviceDAO.getVmDeviceByVmId(vmGuid)).thenReturn(Collections.<VmDevice> emptyList());

        HashMap vm = new HashMap();
        vm.put(VdsProperties.vm_guid, vmGuid.toString());

        Map<String, Object> deviceProperties = new HashMap<String, Object>();
        Guid deviceID = Guid.newGuid();
        deviceProperties.put(VdsProperties.DeviceId, deviceID.toString());
        deviceProperties.put(VdsProperties.Address, Collections.emptyMap());
        deviceProperties.put(VdsProperties.Device, "aDevice");
        deviceProperties.put(VdsProperties.Type, VmDeviceGeneralType.DISK.getValue());

        vm.put(VdsProperties.Devices, new HashMap[] { new HashMap(deviceProperties) });
        vmInfo = new HashMap[] { vm };

        updater.updateVmDevices(Collections.singletonList(vmGuid.toString()));

        assertEquals("wrong number of new devices", 1, updater.getNewVmDevices().size());
        assertEquals("wrong number of removed devices", 0, updater.getRemovedVmDevices().size());
    }

    @Test
    public void updateLunDisksNoMismatch() {
        LUNs lun = new LUNs();
        lun.setLUN_id(Guid.newGuid().toString());
        lun.setDeviceSize(10);

        LunDisk lunDisk = new LunDisk();
        lunDisk.setLun(lun);

        updateLunDisksTest(Collections.singletonMap(lun.getLUN_id(), lun), Collections.singletonList((Disk) lunDisk));

        assertEquals("wrong number of LUNs to update", 0, updater.getVmLunDisksToSave().size());
    }

    @Test
    public void updateLunDisksMismatch() {
        String lunGuid = Guid.newGuid().toString();

        LUNs lun1 = new LUNs();
        lun1.setLUN_id(lunGuid);
        lun1.setDeviceSize(10);

        LUNs lun2 = new LUNs();
        lun2.setLUN_id(lunGuid);
        lun2.setDeviceSize(20);

        LunDisk lunDisk = new LunDisk();
        lunDisk.setLun(lun2);

        updateLunDisksTest(Collections.singletonMap(lun1.getLUN_id(), lun1), Collections.singletonList((Disk) lunDisk));

        assertEquals("wrong number of LUNs to update", 1, updater.getVmLunDisksToSave().size());
    }

    private void updateLunDisksTest(Map<String, LUNs> lunsMapFromVmStats, List<Disk> vmLunDisksFromDb) {
        Guid vmId = Guid.newGuid();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(vmId);
        VmInternalData vmInternalData = new VmInternalData(vmDynamic, null, null, lunsMapFromVmStats);

        when(diskDAO.getAllForVm(any(Guid.class), any(Boolean.class))).thenReturn(vmLunDisksFromDb);

        poweringUpVms = Collections.singletonList(vmDynamic);
        runningVms = Collections.singletonMap(vmId, vmInternalData);

        updater.updateLunDisks();
    }

    private void initConditions() {
        when(dbFacade.getVdsGroupDao()).thenReturn(groupDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDao()).thenReturn(mockAuditLogDao);
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDAO);
        when(dbFacade.getDiskDao()).thenReturn(diskDAO);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        Map<Guid, VM> emptyMap = Collections.emptyMap();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(emptyMap);
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(new Guid("00000000-0000-0000-0000-000000000012"));
    }

    @Test
    public void isNewWatchdogEvent() {
        VmDynamic dynamic = new VmDynamic();
        VM vm = new VM();
        assertFalse(VdsUpdateRunTimeInfo.isNewWatchdogEvent(dynamic, vm));
        dynamic.setLastWatchdogEvent(1L);
        assertTrue(VdsUpdateRunTimeInfo.isNewWatchdogEvent(dynamic, vm));
        vm.setLastWatchdogEvent(1L);
        dynamic.setLastWatchdogEvent(1L);
        assertFalse(VdsUpdateRunTimeInfo.isNewWatchdogEvent(dynamic, vm));
        dynamic.setLastWatchdogEvent(2L);
        assertTrue(VdsUpdateRunTimeInfo.isNewWatchdogEvent(dynamic, vm));
        dynamic.setLastWatchdogEvent(null);
        assertFalse(VdsUpdateRunTimeInfo.isNewWatchdogEvent(dynamic, vm));
    }
}
