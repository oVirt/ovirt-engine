package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EJBUtilsStrategy;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.vdsbroker.AuditLogDaoMocker;
import org.ovirt.engine.core.vdsbroker.MonitoringStrategy;
import org.ovirt.engine.core.vdsbroker.VdsUpdateRunTimeInfo;

@RunWith(MockitoJUnitRunner.class)
public class VdsUpdateRunTimeInfoTest {
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VdsLocalDisksLowFreeSpace, 0),
            mockConfig(ConfigValues.VdsLocalDisksCriticallyLowFreeSpace, 0),
            mockConfig(ConfigValues.VdsRefreshRate, 3000),
            mockConfig(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes, 30),
            mockConfig(ConfigValues.VdsRecoveryTimeoutInMintues, 3)
            );

    private VDS vds;

    VdsUpdateRunTimeInfo updater;

    @Mock
    EjbUtils ejbUtils;

    @Mock
    TransactionManager tm;

    @Mock
    VdsGroupDAO groupDAO;

    @Mock
    VmDAO vmDAO;

    @Mock
    DbFacade dbFacade;

    @Mock
    VDSGroup cluster;

    AuditLogDAO mockAuditLogDao = new AuditLogDaoMocker();

    @Mock
    EJBUtilsStrategy mockStrategy;

    private EJBUtilsStrategy origEjbStrategy;

    @Before
    public void setup() {
        initVds();
        mockEjbStrategy();
        initConditions();
        updater = new VdsUpdateRunTimeInfo(null, vds) {

            @Override
            public DbFacade getDbFacade() {
                return dbFacade;
            }

            @Override
            protected MonitoringStrategy getMonitoringStrategyForVds(VDS param) {
                return mock(MonitoringStrategy.class);
            }

            @Override
            protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
                AuditLog al = new AuditLog();
                al.setlog_type(logType);
                mockAuditLogDao.save(al);
            }
        };
    }

    @Test
    public void testLogMtuDiffs() {
        for (VdsNetworkInterface iface : getInterfaces()) {
            updater.logMTUDifferences(getClustersMap(), iface);
        }
        assertEquals(1, mockAuditLogDao.getAll().size());
    }

    private static List<VdsNetworkInterface> getInterfaces() {
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setMtu(1500);
        nic.setNetworkName("oz");
        ifaces.add(nic);
        return ifaces;
    }

    private static Map<String, network> getClustersMap() {
        Map<String, network> map = new HashMap<String, network>();
        network net = new network();
        net.setname("oz");
        net.setMtu(9000);
        map.put("oz", net);
        return map;
    }

    private void initConditions() {
        when(dbFacade.getVdsGroupDAO()).thenReturn(groupDAO);
        when(dbFacade.getVmDAO()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDAO()).thenReturn(mockAuditLogDao);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        Map<Guid, VM> emptyMap = Collections.emptyMap();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(emptyMap);
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000012"));

    }

    public void mockEjbStrategy() {
        origEjbStrategy = EjbUtils.getStrategy();
        EjbUtils.setStrategy(mockStrategy);
        when(mockStrategy.findResource(ContainerManagedResourceType.TRANSACTION_MANAGER)).thenReturn(tm);
    }

    @After
    public void unmockEjbStrategy() {
        EjbUtils.setStrategy(origEjbStrategy);
    }
}
