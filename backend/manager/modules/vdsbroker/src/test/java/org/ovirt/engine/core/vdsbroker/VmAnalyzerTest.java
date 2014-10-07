package org.ovirt.engine.core.vdsbroker;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmJobDao;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

@RunWith(Theories.class)
public class VmAnalyzerTest {

    @DataPoints
    public static VmTestPairs[] VMS = VmTestPairs.values();

    VmAnalyzer vmAnalyzer;

    @Mock
    VmsMonitoring vmsMonitoring;
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
    private VmStatisticsDAO vmStatisticsDAO;
    @Mock
    private VmStaticDAO vmStaticDAO;
    @Mock
    private VmDynamicDAO vmDynamicDao;
    @Mock
    private VDSGroup vdsGroup;
    @Mock
    private VdsGroupDAO vdsGroupDAO;
    @Mock
    private VdsDAO vdsDAO;
    @Mock
    private VDS srcHost;
    @Mock
    private VDS dstHost;
    @Mock
    private DbFacade dbFacade;
    @Mock
    private VdsManager vdsManager;
    @Mock
    private VDS vdsManagerVds;
    @Mock
    private ResourceManager resourceManager;
    @Mock
    private VmJobDao vmJobsDao;
    @Mock
    private VmDAO vmDAO;
    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Theory
    public void externalVMWhenMissingInDb(VmTestPairs data) {
        //given
        initMocks(data, false);
        mockVmStatic(false);
        mockVmNotInDb(data);
        //when
        assumeTrue(data.dbVm() == null);
        assumeTrue(data.vdsmVm() != null);
        //then
        vmAnalyzer.analyze();
        assertTrue(vmAnalyzer.isExternalVm());
    }

    @Theory
    public void vmNotRunningOnHost(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeTrue(data.vdsmVm() == null);
        //then
        vmAnalyzer.analyze();
        assertTrue(vmAnalyzer.isMovedToDown());
    }

    @Theory
    public void proceedDownVmsNormalExistReason_MIGRATION_HANDOVER(VmTestPairs data) {
        //given
        initMocks(data, false);
        if (data.dbVm() != null) {
            when(vmsMonitoring.getDbFacade()
                    .getVmStatisticsDao()
                    .get(data.dbVm().getId())).thenReturn(data.dbVm().getStatisticsData());
        }

        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() == VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        verify(vmsMonitoring).addVmDynamicToList(data.dbVm().getDynamicData());
        verify(vmsMonitoring.getResourceManager(), never()).RemoveAsyncRunningVm(data.dbVm().getId());
        verify(vmsMonitoring.getResourceManager()).runVdsCommand(vdsCommandTypeCaptor.capture(),
                vdsParamsCaptor.capture());
        assertTrue(vdsCommandTypeCaptor.getValue() == VDSCommandType.Destroy);
        assertTrue(vdsParamsCaptor.getValue().getClass() == DestroyVmVDSCommandParameters.class);
    }

    @Theory
    public void proceedDownVmsNormalExistReason(VmTestPairs data) {
        //given
        initMocks(data, false);
        if (data.dbVm() != null) {
            when(vmsMonitoring.getDbFacade()
                    .getVmStatisticsDao()
                    .get(data.dbVm().getId())).thenReturn(data.dbVm().getStatisticsData());
        }

        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        assumeTrue(data.dbVm().getStatus() != VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() == VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        verify(vmsMonitoring).addVmDynamicToList(data.dbVm().getDynamicData());
        verify(vmsMonitoring.getResourceManager()).RemoveAsyncRunningVm(data.dbVm().getId());
        verify(vmsMonitoring.getResourceManager()).runVdsCommand(vdsCommandTypeCaptor.capture(),
                vdsParamsCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.VM_DOWN));
        assertTrue(vdsCommandTypeCaptor.getValue() == VDSCommandType.Destroy);
        assertTrue(vdsParamsCaptor.getValue().getClass() == DestroyVmVDSCommandParameters.class);
    }

    @Theory
    public void proceedDownVmsErrorExitReason(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        assumeTrue(data.vdsmVm().getVmDynamic().getExitStatus() != VmExitStatus.Normal);
        //then
        vmAnalyzer.analyze();
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        verify(vmsMonitoring.getResourceManager(), atLeast(3)).IsVmInAsyncRunningList(data.dbVm().getId());
        verify(vmsMonitoring).addVmDynamicToList(data.dbVm().getDynamicData());
    }

    @Theory
    public void proceedWatchdogEvents(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        //then
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.WATCHDOG_EVENT));
    }

    @Theory
    public void proceedBalloonCheck(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        //then
        verify(auditLogDirector, atLeastOnce()).log(loggableCaptor.capture(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.WATCHDOG_EVENT));
    }

    @Theory
    public void proceedGuaranteedMemoryCheck() {
        //TODO add tests here
    }

    @Theory
    public void updateRepository_MIGRATION_FROM(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        // when vm is migrating
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.MigratingFrom);
        //then
        assertTrue(vmAnalyzer.isClientIpChanged());
        verify(vmsMonitoring.getResourceManager(), never()).InternalSetVmStatus(data.dbVm(),
                VMStatus.MigratingTo);
    }

    @Theory
    public void updateRepository_MIGRATION_FROM_TO_DOWN(VmTestPairs data) {
        //given
        initMocks(data, true);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        // when vm ended migration
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        //then
        verify(vmsMonitoring.getResourceManager(), times(1)).InternalSetVmStatus(data.dbVm(),
                VMStatus.MigratingTo);
        verify(vmsMonitoring, atLeastOnce()).addVmDynamicToList(data.dbVm().getDynamicData());
        verify(vmsMonitoring, atLeastOnce()).addVmStatisticsToList(data.dbVm().getStatisticsData());
        assertTrue(data.dbVm().getRunOnVds().equals(VmTestPairs.DST_HOST_ID));
    }

    @Theory
    public void updateRepository_MIGRATION_FROM_TO_UP(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        // when migration failed
        assumeTrue(data.dbVm().getStatus() == VMStatus.MigratingFrom);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Up);
        //then
        vmAnalyzer.analyze();
        verify(vmsMonitoring, atLeastOnce()).addVmDynamicToList(data.dbVm().getDynamicData());
        verify(vmsMonitoring.getResourceManager(), times(1)).RemoveVmFromDownVms(VmTestPairs.SRC_HOST_ID, data.vdsmVm().getVmDynamic().getId());
        assertTrue(data.dbVm().getRunOnVds().equals(VmTestPairs.SRC_HOST_ID));
        assertTrue(vmAnalyzer.isRerun());
        assertTrue(data.dbVm().getMigratingToVds() == null);
    }

    @Theory
    public void updateRepository_HA_VM_DOWN(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeNotNull(data.dbVm(), data.vdsmVm());
        // when migration failed
        assumeTrue(data.dbVm().getStatus() == VMStatus.Up);
        assumeTrue(data.dbVm().isAutoStartup());
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Down);
        //then
        vmAnalyzer.analyze();
        verify(vmsMonitoring, atLeastOnce()).addVmDynamicToList(data.dbVm().getDynamicData());
        verify(vmsMonitoring, atLeastOnce()).addVmStatisticsToList(data.dbVm().getStatisticsData());
        assertTrue(data.dbVm().getRunOnVds().equals(VmTestPairs.SRC_HOST_ID));
        assertTrue(data.vdsmVm().getVmDynamic().getRunOnVds() == null);
        assertFalse(vmAnalyzer.isRerun());
        assertTrue(vmAnalyzer.isAutoVmToRun());
        assertTrue(data.dbVm().getMigratingToVds() == null);
    }

    @Theory
    public void updateRepository_PERSIST_DST_UP_VMS(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeNotNull(data.vdsmVm());
        assumeTrue(data.vdsmVm().getVmDynamic().getRunOnVds() == VmTestPairs.DST_HOST_ID);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.Up);
        //then
        vmAnalyzer.analyze();
        verify(vmsMonitoring, times(1)).addVmDynamicToList(Matchers.<VmDynamic>any());
        verify(vmsMonitoring, never()).addVmDynamicToList(data.vdsmVm().getVmDynamic());
    }

    @Theory
    public void updateRepository_PERSIST_ALL_VMS_EXCEPT_MIGRATING_TO(VmTestPairs data) {
        //given
        initMocks(data, false);
        //when
        assumeNotNull(data.vdsmVm());
        assumeTrue(data.vdsmVm().getVmDynamic().getRunOnVds() == VmTestPairs.DST_HOST_ID);
        assumeTrue(data.vdsmVm().getVmDynamic().getStatus() == VMStatus.MigratingTo);
        //then
        vmAnalyzer.analyze();
        verify(vmsMonitoring, never()).addVmDynamicToList(Mockito.<VmDynamic>any());
        verify(vmsMonitoring, never()).addVmDynamicToList(data.vdsmVm().getVmDynamic());
    }

    @Theory
    public void prepareGuestAgentNetworkDevicesForUpdate() {
        // TODO add tests
    }

    @Theory
    public void updateLunDisks() {

    }

    @Before
    public void before() {
        for (VmTestPairs data: VmTestPairs.values()) {
            data.reset();
        }
        MockitoAnnotations.initMocks(this);
    }

    public void initMocks(VmTestPairs vmData, boolean run) {
        stubDaos();
        when(vdsManager.getVdsId()).thenReturn(VmTestPairs.SRC_HOST_ID);
        when(vdsManager.getVdsGroupId()).thenReturn(VmTestPairs.CLUSTER_ID);
        when(vdsManager.getCopyVds()).thenReturn(vdsManagerVds);
        when(vmsMonitoring.getVdsManager()).thenReturn(vdsManager);
        when(vmsMonitoring.getResourceManager()).thenReturn(resourceManager);
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);
        when(vmsMonitoring.getResourceManager().runVdsCommand((VDSCommandType) anyObject(),
                (org.ovirt.engine.core.common.vdscommands.VDSParametersBase) anyObject())).thenReturn(
                vdsReturnValue);
        // -- default behaviors --
        // dst host is up
        mockDstHostStatus(VDSStatus.Up);
        // dst VM is in DB under the same Guid
        mockVmInDbForDstVms(vmData);
        // -- end of behaviors --
        vmAnalyzer = new VmAnalyzer(vmData.dbVm(), vmData.vdsmVm(), vmsMonitoring, auditLogDirector);
        if (run) {
            vmAnalyzer.analyze();
        }
    }

    private void stubDaos() {
        mockStatistics();
        mockVmDynamic();
        mockVmStatic(true);
        mockVmJob();
        mockCluster();
        mockVdsDao();
        doReturn(dbFacade).when(vmsMonitoring).getDbFacade();
        doReturn(vmStatisticsDAO).when(dbFacade).getVmStatisticsDao();
        doReturn(vmDynamicDao).when(dbFacade).getVmDynamicDao();
        doReturn(vmStaticDAO).when(dbFacade).getVmStaticDao();
        doReturn(vmNetworkInterfaceDao).when(dbFacade).getVmNetworkInterfaceDao();
        doReturn(vmJobsDao).when(dbFacade).getVmJobDao();
        doReturn(vdsGroupDAO).when(dbFacade).getVdsGroupDao();
        doReturn(vdsDAO).when(dbFacade).getVdsDao();
        doReturn(vmDAO).when(dbFacade).getVmDao();
    }

    private void mockStatistics() {
        when(vmStatisticsDAO.get((Guid) anyObject())).thenReturn(mock(VmStatistics.class));
    }

    private void mockVmDynamic() {
        when(vmDynamicDao.get((Guid) anyObject())).thenReturn(mock(VmDynamic.class));
    }

    private void mockVmStatic(boolean stubExists) {
        Mockito.reset(vmStaticDAO);
        when(vmStaticDAO.get((Guid) anyObject())).thenReturn(stubExists ? mock(VmStatic.class) : null);
    }

    private void mockVmJob() {
        Mockito.reset(vmJobsDao);
        when(vmJobsDao.get((Guid) anyObject())).thenReturn(mock(VmJob.class));
    }

    private void mockCluster() {
        when(vdsGroupDAO.get(VmTestPairs.CLUSTER_ID)).thenReturn(vdsGroup);
    }

    private void mockVdsDao() {
        when(vdsDAO.get(VmTestPairs.SRC_HOST_ID)).thenReturn(srcHost);
        when(vdsDAO.get(VmTestPairs.DST_HOST_ID)).thenReturn(dstHost);
    }

    private void mockDstHostStatus(VDSStatus status) {
        when(dstHost.getStatus()).thenReturn(status);
    }


    private void mockVmInDbForDstVms(VmTestPairs vmData) {
        if (vmData.dbVm() == null && vmData.vdsmVm() != null) {
            VM dbVm = vmData.createDbVm();
            when(vmDAO.get(vmData.vdsmVm().getVmDynamic().getId()))
                    .thenReturn(dbVm);
        }
    }
    private void mockVmNotInDb(VmTestPairs vmData) {
        if (vmData.vdsmVm() != null) {
            when(vmDAO.get(vmData.vdsmVm().getVmDynamic().getId()))
                    .thenReturn(null);
        }
    }
}
