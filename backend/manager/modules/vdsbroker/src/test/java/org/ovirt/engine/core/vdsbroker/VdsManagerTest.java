package org.ovirt.engine.core.vdsbroker;

/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.ovirt.engine.core.common.businessentities.VDS;
//import org.ovirt.engine.core.common.businessentities.VDSGroup;
//import org.ovirt.engine.core.common.businessentities.VDSStatus;
//import org.ovirt.engine.core.common.config.Config;
//import org.ovirt.engine.core.common.config.ConfigValues;
//import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
//import org.ovirt.engine.core.compat.Guid;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.dao.VdsGroupDAO;
//import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ DbFacade.class, SchedulerUtilQuartzImpl.class })
//public class VdsManagerTest {
//    @Mock
//    private DbFacade db;
//
//    @Mock
//    private SchedulerUtilQuartzImpl schedUtil;
//
//    @Mock
//    private VdsGroupDAO vdsGroupDAO;
//
//    public VdsManagerTest() {
//        MockitoAnnotations.initMocks(this);
//        mockStatic(DbFacade.class);
//        mockStatic(SchedulerUtilQuartzImpl.class);
//    }
//
//    private void mockDbFacade() {
//        mockStatic(DbFacade.class);
//        when(DbFacade.getInstance()).thenReturn(db);
//        when(db.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
//        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(mockVirtVDSGroup());
//    }
//
//    private VDSGroup mockVirtVDSGroup() {
//        VDSGroup vdsGroup = new VDSGroup();
//        return vdsGroup;
//    }
//
//    public void mockSchedulerUtil() {
//        mockStatic(SchedulerUtilQuartzImpl.class);
//        when(SchedulerUtilQuartzImpl.getInstance()).thenReturn(schedUtil);
//    }
//
//    public void mockConfig() {
//        IConfigUtilsInterface mockConfigUtils = mock(IConfigUtilsInterface.class);
//        Config.setConfigUtils(mockConfigUtils);
//
//        when(mockConfigUtils.<Integer> getValue(ConfigValues.VdsRefreshRate, ConfigCommon.defaultConfigurationVersion)).thenReturn(60);
//        when(mockConfigUtils.<Integer> getValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes, ConfigCommon.defaultConfigurationVersion)).thenReturn(60);
//        when(mockConfigUtils.<Integer> getValue(ConfigValues.VdsRecoveryTimeoutInMinutes, ConfigCommon.defaultConfigurationVersion)).thenReturn(60);
//        when(mockConfigUtils.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave, ConfigCommon.defaultConfigurationVersion)).thenReturn(1);
//        when(mockConfigUtils.<Integer> getValue(ConfigValues.vdsTimeout, ConfigCommon.defaultConfigurationVersion)).thenReturn(60);
//        when(mockConfigUtils.<Boolean> getValue(ConfigValues.EncryptHostCommunication, ConfigCommon.defaultConfigurationVersion)).thenReturn(false);
//    }
//
//    public void setMockups() {
//        mockConfig();
//        mockDbFacade();
//        mockSchedulerUtil();
//    }
//
//    @Test
//    public void testVdsManagerIsMonitoringNeeded() {
//        setMockups();
//        VDS vds = new VDS();
//        VdsManager vdsManager = VdsManager.buildVdsManager(vds);
//        vds.setStatus(VDSStatus.NonOperational);
//        vds.setVmCount(1);
//        vdsManager.setVds(vds);
//        assertTrue(vdsManager.isMonitoringNeeded());
//        vds.setVmCount(0);
//        assertFalse(vdsManager.isMonitoringNeeded());
//        vds.setStatus(VDSStatus.Up);
//        assertTrue(vdsManager.isMonitoringNeeded());
//    }
//
//    @Test
//    public void testCreateVdsManager() {
//        VDS vds = new VDS();
//        setMockups();
//        // Check no exceptions and such
//        VdsManager vdsManager = VdsManager.buildVdsManager(vds);
//    }
// }
