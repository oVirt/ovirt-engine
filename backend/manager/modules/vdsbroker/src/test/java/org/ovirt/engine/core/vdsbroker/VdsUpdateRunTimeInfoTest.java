package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@RunWith(MockitoJUnitRunner.class)
public class VdsUpdateRunTimeInfoTest {

    private static final Guid VM_1 = Guid.createGuidFromString("7eeabc50-325f-49bb-acb6-15e786599423");
    private static final Version vdsCompVersion = Version.v3_4;

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            MockConfigRule.mockConfig(
                    ConfigValues.DebugTimerLogging,
                    true),
            MockConfigRule.mockConfig(
                    ConfigValues.VdsRefreshRate,
                    3),
            MockConfigRule.mockConfig(
                    ConfigValues.ReportedDisksLogicalNames,
                    vdsCompVersion.getValue(),
                    true),
            MockConfigRule.mockConfig(
                    ConfigValues.TimeToReduceFailedRunOnVdsInMinutes,
                    3)
    );

    private VDS vds;
    HashMap[] vmInfo;
    List<VmDynamic> poweringUpVms;

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

    @Mock
    VmDynamicDAO vmDynamicDao;

    AuditLogDAO mockAuditLogDao = new AuditLogDaoMocker();

    VM vm_1_db;
    VM vm_1_vdsm;

    @Mock
    ResourceManager resourceManager;

    @Mock
    private VdsManager vdsManager;


    @Before
    public void setup() {
        initVds();
        initConditions();
        when(vdsManager.getRefreshStatistics()).thenReturn(false);
        updater = Mockito.spy(
                    new VdsUpdateRunTimeInfo(vdsManager, vds, mock(MonitoringStrategy.class)) {

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
        });
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
        String testLogicalName = "TestName";
        vm.put(VdsProperties.vm_guid, vmGuid.toString());

        Map<String, Object> deviceProperties = new HashMap<String, Object>();
        Guid deviceID = Guid.newGuid();
        deviceProperties.put(VdsProperties.DeviceId, deviceID.toString());
        deviceProperties.put(VdsProperties.Address, Collections.emptyMap());
        deviceProperties.put(VdsProperties.Device, VmDeviceType.DISK.getName());
        deviceProperties.put(VdsProperties.Type, VmDeviceGeneralType.DISK.getValue());
        vm.put(VdsProperties.GuestDiskMapping,
                Collections.singletonMap(deviceID.toString().substring(0, 20),
                        Collections.singletonMap(VdsProperties.Name, testLogicalName)));

        vm.put(VdsProperties.Devices, new HashMap[] { new HashMap(deviceProperties) });
        vmInfo = new HashMap[] { vm };

        updater.updateVmDevices(Collections.singletonList(vmGuid.toString()));

        assertEquals("wrong number of new devices", 1, updater.getNewVmDevices().size());
        VmDevice device = updater.getNewVmDevices().get(0);
        assertEquals(testLogicalName, device.getLogicalName());
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
        when(updater.getRunningVms()).thenReturn(Collections.singletonMap(vmId, vmInternalData));
        poweringUpVms = Collections.singletonList(vmDynamic);

        updater.updateLunDisks();
    }

    private void initConditions() {
        when(dbFacade.getVdsGroupDao()).thenReturn(groupDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDao()).thenReturn(mockAuditLogDao);
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDAO);
        when(dbFacade.getVmDynamicDao()).thenReturn(vmDynamicDao);
        when(dbFacade.getDiskDao()).thenReturn(diskDAO);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        initVm();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(Collections.singletonMap(VM_1, vm_1_db));
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(new Guid("00000000-0000-0000-0000-000000000012"));
        vds.setVdsGroupCompatibilityVersion(vdsCompVersion);
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

    /**
     * Test that when we succeed in retriving a VM stats, we insert it into the internal runningVms structure
     */
    @Test
    public void verifyListOfRunningVmsIsSameWithSuccessFromVdsmResponse() {
        prepareForRefreshVmStatsCall();
        mockGetVmStatsCommand(true);
        // start refreshing vm data... VURTI now fetches Vms list from ResourceManager and loop through it
        updater.fetchRunningVms();
        List<Guid> staleRunningVms = updater.checkVmsStatusChanged();

        Assert.assertTrue(updater.getRunningVms().containsKey(VM_1));
        Assert.assertFalse(staleRunningVms.contains(VM_1));
    }

    /**
     * Test that when we fail in getting a response for GetVmStats we still insert the VM to the runningVms structure,<br>
     * but not the internal runningVmStructure which is the same handling as if vm status didn't change
     */
    @Test
    public void verifyListOfRunningVmsIsSameWithFailureOnGetVmStats() {
        prepareForRefreshVmStatsCall();
        mockGetVmStatsCommand(false);
        // start refreshing vm data... VURTI now fetches Vms list from ResourceManager and loop through it
        updater.fetchRunningVms();
        List<Guid> staleRunningVms= updater.checkVmsStatusChanged();

        Assert.assertFalse(updater.getRunningVms().containsKey(VM_1));
        Assert.assertTrue(staleRunningVms.contains(VM_1));
    }

    private void prepareForRefreshVmStatsCall() {
        initVm();
        mockUpdater();
        mockListCommand();
    }

    private void mockUpdater() {
        when(updater.getResourceManager()).thenReturn(resourceManager);
    }

    private void initVm() {
        vm_1_vdsm = new VM();
        vm_1_db = new VM();
        vm_1_db.setId(VM_1);
        vm_1_vdsm.setId(VM_1);
        vm_1_db.setStatus(VMStatus.WaitForLaunch);
        vm_1_vdsm.setStatus(VMStatus.Up);
        vm_1_db.setName("vm-prod-mailserver");
        vm_1_vdsm.setName("vm-prod-mailserver");
        when(vmDynamicDao.get(VM_1)).thenReturn(vm_1_db.getDynamicData());
    }

    private void mockGetVmStatsCommand(boolean mockSuccess) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        if (mockSuccess) {
            vdsReturnValue.setSucceeded(true);
            vdsReturnValue.setReturnValue(createRunningVms().get(VM_1));
        }
        when(resourceManager.runVdsCommand(Mockito.eq(VDSCommandType.GetVmStats), (VDSParametersBase) Mockito.anyObject())).thenReturn(vdsReturnValue);
    }

    private void mockListCommand() {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);
        vdsReturnValue.setReturnValue(createRunningVms());
        when(resourceManager.runVdsCommand(Mockito.eq(VDSCommandType.List), (VDSParametersBase) Mockito.anyObject())).thenReturn(vdsReturnValue);
        when(resourceManager.runVdsCommand(Mockito.eq(VDSCommandType.GetVmStats), (VDSParametersBase) Mockito.anyObject())).thenReturn(vdsReturnValue);
        when(updater.getResourceManager()).thenReturn(resourceManager);
    }

    private Map<Guid, VmInternalData> createRunningVms() {
        HashMap<Guid, VmInternalData> vms = new HashMap<>();
        vms.put(VM_1, new VmInternalData(vm_1_vdsm.getDynamicData(), null, null, null));
        return vms;
    }
}
