package org.ovirt.engine.core.vdsbroker;

/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */


//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.ovirt.engine.core.common.businessentities.VDS;
//import org.ovirt.engine.core.common.businessentities.VDSGroup;
//import org.ovirt.engine.core.compat.Guid;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.dao.VdsGroupDao;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ DbFacade.class})
//public class MonitoringStrategyFactoryTest {
//    @Mock
//    private DbFacade db;
//
//    @Mock
//    private VdsGroupDao vdsGroupDao;
//
//    @Before
//    public void mockDbFacade() {
//        mockStatic(DbFacade.class);
//        when(DbFacade.getInstance()).thenReturn(db);
//        when(db.getVdsGroupDao()).thenReturn(vdsGroupDao);
//        when(vdsGroupDao.get(any(Guid.class))).thenReturn(mockVirtVDSGroup()).thenReturn(mockGlusterVDSGroup()).thenReturn(mockBothVDSGroup());
//    }
//
//    private VDSGroup mockVirtVDSGroup() {
//        VDSGroup vdsGroup = new VDSGroup();
//        vdsGroup.setGlusterService(false);
//        vdsGroup.setVirtService(true);
//        return vdsGroup;
//    }
//
//    private VDSGroup mockGlusterVDSGroup() {
//        VDSGroup vdsGroup = new VDSGroup();
//        vdsGroup.setGlusterService(true);
//        vdsGroup.setVirtService(false);
//        return vdsGroup;
//    }
//
//    private VDSGroup mockBothVDSGroup() {
//        VDSGroup vdsGroup = new VDSGroup();
//        vdsGroup.setGlusterService(true);
//        vdsGroup.setVirtService(true);
//        return vdsGroup;
//    }
//
//    @Test
//    public void testMonitoringStrategyFactoryVirtStrategy() {
//        VDS vds = new VDS();
//        MonitoringStrategy monitoringStrategy = MonitoringStrategyFactory.getMonitoringStrategyForVds(vds);
//        assertTrue(monitoringStrategy instanceof VirtMonitoringStrategy);
//    }
//
//    @Test
//    public void testMonitoringStrategyFactoryGlusterStrategy() {
//        VDS vds = new VDS();
//        MonitoringStrategy monitoringStrategy = MonitoringStrategyFactory.getMonitoringStrategyForVds(vds);
//        assertTrue(monitoringStrategy instanceof GlusterMonitoringStrategy);
//    }
//
//    @Test
//    public void testMonitoringStrategyFactoryBothStrategy() {
//        VDS vds = new VDS();
//        MonitoringStrategy monitoringStrategy = MonitoringStrategyFactory.getMonitoringStrategyForVds(vds);
//        assertTrue(monitoringStrategy instanceof MultipleServicesMonitoringStrategy);
//    }
//
// }
