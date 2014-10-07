package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@Ignore
@RunWith(MockitoJUnitRunner.class)
/**
 * @Ignore
 * most of the functionally is tested in {@link org.ovirt.engine.core.vdsbroker.VmAnalyzerTest}
 */
public class VmsMonitoringTest {

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
                    ConfigValues.TimeToReduceFailedRunOnVdsInMinutes,
                    3),
            MockConfigRule.mockConfig(
                    ConfigValues.ReportedDisksLogicalNames,
                    vdsCompVersion.getValue(),
                    true));

    private VDS vds;
    HashMap[] vmInfo;
    List<VmDynamic> poweringUpVms;

    VmsMonitoring vmsMonitoring;

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

    @Mock
    private VdsDAO vdsDao;

    AuditLogDAO mockAuditLogDao = new AuditLogDaoMocker();

    VM vm_1_db;
    VM vm_1_vdsm;

    @Mock
    ResourceManager resourceManager;

    @Mock
    AuditLogDirector auditLogDirector;

    @Mock
    private VdsManager vdsManager;

    @Before
    public void setup() {
        initVds();
        initConditions();
        when(vdsManager.getRefreshStatistics()).thenReturn(false);
        vmsMonitoring = Mockito.spy(
                new VmsMonitoring(
                        vdsManager,
                        Arrays.asList(VmTestPairs.MIGRATION_DONE.build()),
                        Collections.<Pair<VM, VmInternalData>>emptyList(), auditLogDirector) {

                    @Override
                    public DbFacade getDbFacade() {
                        return dbFacade;
                    }

                    @Override
                    protected Map[] getVmInfo(List<String> vmsToUpdate) {
                        return vmInfo;
                    }

                    @Override
                    protected List<VmDynamic> getPoweringUpVms() {
                        return poweringUpVms;
                    }
                }
        );
    }

    private void initConditions() {
        when(dbFacade.getVdsGroupDao()).thenReturn(groupDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDao()).thenReturn(mockAuditLogDao);
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDAO);
        when(dbFacade.getVmDynamicDao()).thenReturn(vmDynamicDao);
        when(dbFacade.getDiskDao()).thenReturn(diskDAO);
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(Collections.singletonMap(VM_1, vm_1_db));
        when(vdsDao.get((Guid) any())).thenReturn(vds);
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(new Guid("00000000-0000-0000-0000-000000000012"));
        vds.setVdsGroupCompatibilityVersion(vdsCompVersion);
    }
}
