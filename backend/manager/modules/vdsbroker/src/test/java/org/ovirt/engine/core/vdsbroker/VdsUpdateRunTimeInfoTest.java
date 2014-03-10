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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDAO;
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

    private void initConditions() {
        when(dbFacade.getVdsGroupDao()).thenReturn(groupDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDao()).thenReturn(mockAuditLogDao);
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDAO);
        when(dbFacade.getVmDynamicDao()).thenReturn(vmDynamicDao);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        Map<Guid, VM> emptyMap = Collections.emptyMap();
        initVm();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(Collections.singletonMap(VM_1, vm_1_db));
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(new Guid("00000000-0000-0000-0000-000000000012"));
        vds.setVdsGroupCompatibilityVersion(Version.v3_3);
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
        vms.put(VM_1, new VmInternalData(vm_1_vdsm.getDynamicData(), null, null));
        return vms;
    }
}
