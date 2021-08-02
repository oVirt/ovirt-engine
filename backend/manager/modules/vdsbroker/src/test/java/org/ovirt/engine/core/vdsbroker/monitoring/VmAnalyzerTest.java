package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmAnalyzerTest {
    VmAnalyzer vmAnalyzer;

    @Mock
    private AuditLogDirector auditLogDirector;
    @Captor
    private ArgumentCaptor<AuditLogableBase> loggableCaptor;
    @Captor
    private ArgumentCaptor<AuditLogType> logTypeCaptor;
    @Captor
    private ArgumentCaptor<VDSCommandType> vdsCommandTypeCaptor;
    @Captor
    private ArgumentCaptor<VDSParametersBase> vdsParamsCaptor;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VdsDynamic srcHost;
    @Mock
    private VdsDynamic dstHost;
    @Mock
    private VdsManager vdsManager;
    @Mock
    private VmManager vmManager;
    @Mock
    private VDS vdsManagerVds;
    @Mock
    private ResourceManager resourceManager;

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void externalVMWhenMissingInDb(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.dbVm() == null);
        assumeTrue(data.vdsmVm() != null
                && data.vdsmVm().getVmDynamic().getStatus() != VMStatus.Down
                && (data.vdsmVm().getVmDynamic().getStatus() != VMStatus.Paused ||
                data.vdsmVm().getVmDynamic().getPauseStatus() != VmPauseStatus.EIO));
        //then
        vmAnalyzer.analyze();
        assertTrue(vmAnalyzer.isUnmanagedVm());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void vmNotRunningOnHost(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.vdsmVm() == null);
        //then
        vmAnalyzer.analyze();
        assertTrue(vmAnalyzer.isMovedToDown());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void proceedDownVmsNormalExistReason_MIGRATION_HANDOVER(VmTestPairs data) {
        //given
        initMocks(data, false);

        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitReason() == VmExitReason.MigrationSucceeded);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() == VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(resourceManager, never()).removeAsyncRunningVm(data.dbVm().getId());
        verify(vmAnalyzer).runVdsCommand(vdsCommandTypeCaptor.capture(), vdsParamsCaptor.capture());
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
        assertEquals(VDSCommandType.Destroy, vdsCommandTypeCaptor.getValue());
        assertEquals(DestroyVmVDSCommandParameters.class, vdsParamsCaptor.getValue().getClass());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void proceedDownVmsNormalExistReason(VmTestPairs data) {
        //given
        initMocks(data, false);

        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm().getStatus() != VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() == VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        verify(resourceManager).removeAsyncRunningVm(data.dbVm().getId());
        verify(vmAnalyzer).runVdsCommand(vdsCommandTypeCaptor.capture(), vdsParamsCaptor.capture());
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.VM_DOWN));
        assertEquals(VDSCommandType.Destroy, vdsCommandTypeCaptor.getValue());
        assertEquals(DestroyVmVDSCommandParameters.class, vdsParamsCaptor.getValue().getClass());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void proceedDownVmsErrorExitReason(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() != VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void proceedWatchdogEvents(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeFalse(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeFalse(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.MigratingTo);
        assumeTrue(Objects.equals(data.vdsmVm().getVmDynamic().getRunOnVds(), data.dbVm().getRunOnVds()));
        //then
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.WATCHDOG_EVENT));
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void vmNotRunningOnHostWithBalloonEnabled(VmTestPairs data) {
        //given
        initMocks(data, false);
        when(vdsManagerVds.isBalloonEnabled()).thenReturn(true);
        //when
        assumeTrue(data.vdsmVm() == null);
        //then
        vmAnalyzer.analyze();
        assertTrue(vmAnalyzer.isMovedToDown());
    }

    @Test
    public void proceedGuaranteedMemoryCheck() {
        //TODO add tests here
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_MIGRATION_FROM(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        // when vm is migrating
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.MigratingFrom);
        //then
        verify(resourceManager, never()).internalSetVmStatus(data.dbVm().getDynamicData(), VMStatus.MigratingTo);
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_MIGRATION_FROM_TO_DOWN(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        // when vm ended migration
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitReason() == VmExitReason.MigrationSucceeded);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        //then
        verify(resourceManager, times(1)).internalSetVmStatus(data.dbVm().getDynamicData(), VMStatus.MigratingTo);
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
        assertEquals(VmTestPairs.DST_HOST_ID, data.dbVm().getRunOnVds());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_MIGRATION_FROM_TO_UP(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        // when migration failed
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Up);
        //then
        vmAnalyzer.analyze();
        verify(resourceManager, times(1)).removeVmFromDownVms(VmTestPairs.SRC_HOST_ID,
                data.vdsmVm().getVmDynamic().getId());
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
        assertEquals(VmTestPairs.SRC_HOST_ID, data.dbVm().getRunOnVds());
        assertTrue(vmAnalyzer.isRerun());
        assertNull(data.dbVm().getMigratingToVds());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_HA_VM_DOWN(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm().getStatus() == VMStatus.Up);
        assumeTrue(data.dbVm().isAutoStartup());
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        //then
        vmAnalyzer.analyze();
        assertEquals(data.dbVm().getDynamicData(), vmAnalyzer.getVmDynamicToSave());
        assertNotNull(vmAnalyzer.getVmStatisticsToSave());
        assertFalse(vmAnalyzer.isRerun());
        assertTrue(vmAnalyzer.isAutoVmToRun());
        assertNull(data.dbVm().getMigratingToVds());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_PERSIST_DST_UP_VMS(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.vdsmVm().getVmDynamic().getRunOnVds() == VmTestPairs.DST_HOST_ID);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Up);
        //then
        vmAnalyzer.analyze();
        assertNotNull(vmAnalyzer.getVmDynamicToSave());
        assertNotEquals(data.vdsmVm().getVmDynamic(), vmAnalyzer.getVmDynamicToSave());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void updateRepository_PERSIST_ALL_VMS_EXCEPT_MIGRATING_TO(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.vdsmVm().getVmDynamic().getRunOnVds() == VmTestPairs.DST_HOST_ID);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.MigratingTo);
        //then
        vmAnalyzer.analyze();
        assertNull(vmAnalyzer.getVmDynamicToSave());
    }

    @Test
    public void prepareGuestAgentNetworkDevicesForUpdate() {
        // TODO add tests
    }

    @BeforeEach
    public void before() {
        for (VmTestPairs data: VmTestPairs.values()) {
            data.reset();
        }
    }

    public void initMocks(VmTestPairs vmData, boolean run) {
        stubDaos();
        when(vdsManager.getVdsId()).thenReturn(VmTestPairs.SRC_HOST_ID);
        when(vdsManager.getClusterId()).thenReturn(VmTestPairs.CLUSTER_ID);
        when(vdsManager.getCopyVds()).thenReturn(vdsManagerVds);
        when(vmManager.isColdReboot()).thenReturn(false);
        when(vmManager.isAutoStart()).thenReturn(vmData.dbVm() != null ? vmData.dbVm().isAutoStartup() : false);
        when(vmManager.getStatistics()).thenReturn(new VmStatistics());
        when(vmManager.getOrigin()).thenReturn(OriginType.OVIRT);
        when(resourceManager.getVdsManager(any())).thenReturn(vdsManager);
        // -- default behaviors --
        // dst host is up
        mockDstHostStatus(VDSStatus.Up);
        // -- end of behaviors --
        vmAnalyzer = spy(new VmAnalyzer(
                vmData.dbVm() != null ? vmData.dbVm().getDynamicData() : null,
                vmData.vdsmVm(),
                false,
                vdsManager,
                auditLogDirector,
                resourceManager,
                vdsDynamicDao,
                null));
        doNothing().when(vmAnalyzer).resetVmInterfaceStatistics();
        doReturn(vmManager).when(vmAnalyzer).getVmManager();
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);
        doReturn(vdsReturnValue).when(vmAnalyzer).runVdsCommand(any(), any());
        doReturn(true).when(vmAnalyzer).saveVmExternalData();

        if (run) {
            vmAnalyzer.analyze();
        }
    }

    private void stubDaos() {
        mockVdsDao();
    }

    private void mockVdsDao() {
        when(vdsDynamicDao.get(VmTestPairs.SRC_HOST_ID)).thenReturn(srcHost);
        when(vdsDynamicDao.get(VmTestPairs.DST_HOST_ID)).thenReturn(dstHost);
    }

    private void mockDstHostStatus(VDSStatus status) {
        when(dstHost.getStatus()).thenReturn(status);
    }

}
