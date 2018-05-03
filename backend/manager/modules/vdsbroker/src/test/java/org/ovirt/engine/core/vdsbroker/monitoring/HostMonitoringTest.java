package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;

/**
 Host and Vms Monitoring now split to 2 classes - all VMs related tests have been move to {@link org.ovirt.engine.core.vdsbroker.monitoring.VmAnalyzerTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class HostMonitoringTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(MockConfigDescriptor.of(ConfigValues.DebugTimerLogging, true));

    @Mock
    private VDS vds;
    @Mock
    InterfaceDao interfaceDao;
    @Mock
    Cluster cluster;
    @Mock
    ResourceManager resourceManager;
    @Mock
    private VdsManager vdsManager;
    @Mock
    private AuditLogDirector auditLogDirector;
    @Mock
    private MonitoringStrategy monitoringStrategy;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Mock
    private NetworkDao networkDao;
    @InjectMocks
    private HostMonitoring updater;

    @Before
    public void initVds() {
        when(vds.getId()).thenReturn(new Guid("00000000-0000-0000-0000-000000000012"));
    }

    /**
     * not an integration test - just test a Network exceptionn doesn't throw and exception
     */
    @Test
    public void testErrorHandling() {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(false);
        value.setExceptionObject(new VDSNetworkException("unknown host"));
        when(resourceManager.runVdsCommand(any(), any())).thenReturn(value);

        updater.refreshVdsStats(true);
    }
}
