package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.AuditLogDaoMocker;
import org.ovirt.engine.core.vdsbroker.VdsUpdateRunTimeInfo;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class, DbFacade.class, TransactionSupport.class, EjbUtils.class, AuditLogDirector.class })
public class VdsUpdateRunTimeInfoTest {

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

    @Before
    public void setup() {
        initVds();
        initStaticMocks();
        spy(TransactionSupport.class);
        spy(AuditLogDirector.class);
        mockConfigVals();
        MockitoAnnotations.initMocks(this);
        initConditions();
        updater = spy(new VdsUpdateRunTimeInfo(null, vds));
    }

    @Test
    public void testLogMtuDiffs() {
        for (VdsNetworkInterface iface : getInterfaces()) {
            updater.logMTUDifferences(getClustersMap(), iface);
        }
        System.out.println(mockAuditLogDao.getAll().get(0).getmessage());
        Assert.assertTrue(mockAuditLogDao.getAll().size() == 1);
    }

    private List<VdsNetworkInterface> getInterfaces() {
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setMtu(1500);
        nic.setNetworkName("oz");
        ifaces.add(nic);
        return ifaces;
    }

    private Map<String, network> getClustersMap() {
        Map<String, network> map = new HashMap<String, network>();
        network net = new network();
        net.setname("oz");
        net.setMtu(9000);
        map.put("oz", net);
        return map;
    }

    private void initConditions() {
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getVdsGroupDAO()).thenReturn(groupDAO);
        when(dbFacade.getVmDAO()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDAO()).thenReturn(mockAuditLogDao);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        when(ejbUtils.findResource(ContainerManagedResourceType.TRANSACTION_MANAGER)).thenReturn(tm);
        Map<Guid, VM> emptyMap = Collections.emptyMap();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(emptyMap);
    }

    private void initStaticMocks() {
        mockStatic(DbFacade.class);
        mockStatic(Config.class);
        mockStatic(TransactionSupport.class);
        mockStatic(EjbUtils.class);
        mockStatic(AuditLogDirector.class);
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000012"));

    }

    private void mockConfigVals() {
        when(Config.<Integer> GetValue(ConfigValues.VdsLocalDisksLowFreeSpace)).thenReturn(0);
        when(Config.<Integer> GetValue(ConfigValues.VdsLocalDisksCriticallyLowFreeSpace)).thenReturn(0);
        when(Config.<Integer> GetValue(ConfigValues.VdsRefreshRate)).thenReturn(3000);
        when(Config.<Integer> GetValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes)).thenReturn(30);
        when(Config.<Integer> GetValue(ConfigValues.VdsRecoveryTimeoutInMintues)).thenReturn(3);
    }

}
