package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;

/**
 Host and Vms Monitoring now split to 2 classes - all VMs related tests have been move to {@link org.ovirt.engine.core.vdsbroker.monitoring.VmAnalyzerTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class HostMonitoringTest {

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
    private HostMonitoring updater;

    @Mock
    ClusterDao groupDao;
    @Mock
    InterfaceDao interfaceDao;
    @Mock
    DbFacade dbFacade;
    @Mock
    Cluster cluster;
    @Mock
    ResourceManager resourceManager;
    @Mock
    private VdsManager vdsManager;
    @Mock
    private IVdsEventListener vdsEventlistener;
    @Mock
    private AuditLogDirector auditLogDirector;

    @Before
    public void setup() {
        initVds();
        initConditions();
        when(vdsManager.isTimeToRefreshStatistics()).thenReturn(false);
        updater =
                new HostMonitoring(vdsManager,
                        vds,
                        mock(MonitoringStrategy.class),
                        resourceManager,
                        dbFacade,
                        auditLogDirector);
    }

    private void initConditions() {
        when(dbFacade.getClusterDao()).thenReturn(groupDao);
        when(groupDao.get(any(Guid.class))).thenReturn(cluster);
        when(dbFacade.getInterfaceDao()).thenReturn(interfaceDao);
        when(interfaceDao.getAllInterfacesForVds(any(Guid.class))).thenReturn(Collections.<VdsNetworkInterface>emptyList());
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(new Guid("00000000-0000-0000-0000-000000000012"));
    }

    /**
     * not an integration test - just test a Network exceptionn doesn't throw and exception
     */
    @Test(expected = VDSNetworkException.class)
    public void testErrorHandling() {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(false);
        value.setExceptionObject(new VDSNetworkException("unknown host"));
        when(resourceManager.getEventListener()).thenReturn(vdsEventlistener);
        when(resourceManager.runVdsCommand(any(VDSCommandType.class),
                any(VDSParametersBase.class))).thenReturn(value);

        updater.refreshVdsStats();
    }
}
