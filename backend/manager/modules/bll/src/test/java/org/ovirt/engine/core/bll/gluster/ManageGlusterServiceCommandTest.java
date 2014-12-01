package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class ManageGlusterServiceCommandTest {
    ManageGlusterServiceCommand cmd;

    @Mock
    GlusterServerServiceDao serverServiceDao;
    @Mock
    GlusterServiceDao serviceDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    ClusterUtils clusterUtils;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 100));

    private final Guid startedServiceId1 = Guid.newGuid();
    private final Guid startedServiceId2 = Guid.newGuid();
    private final Guid stoppedServiceId1 = Guid.newGuid();
    private final Guid stoppedServiceId2 = Guid.newGuid();

    private void prepareMocks(ManageGlusterServiceCommand command) {
        doReturn(serverServiceDao).when(command).getGlusterServerServiceDao();
        doReturn(serviceDao).when(command).getGlusterServiceDao();
        doReturn(clusterUtils).when(command).getClusterUtils();
        doReturn(getUpServers()).when(clusterUtils).getAllUpServers(any(Guid.class));
        doReturn(null).when(serverServiceDao).getByServerIdAndServiceType(null, null);
        doReturn(null).when(serviceDao).getByServiceType(null);
    }

    private List<VDS> getUpServers() {
        final VDS vds1 = new VDS();
        vds1.setId(Guid.newGuid());
        vds1.setVdsName("gfs1");
        vds1.setVdsGroupId(Guid.newGuid());
        vds1.setStatus(VDSStatus.Up);
        final VDS vds2 = new VDS();
        vds2.setId(Guid.newGuid());
        vds2.setVdsName("gfs2");
        vds2.setVdsGroupId(Guid.newGuid());
        vds2.setStatus(VDSStatus.Up);

        return new ArrayList<VDS>() {
            {
                add(vds1);
                add(vds2);
            }
        };
    }

    private void mockBackend(boolean succeeded, VdcBllErrors errorCode, GlusterServiceStatus status) {
        doReturn(backend).when(cmd).getBackend();
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        } else {
            if (status == GlusterServiceStatus.STOPPED) {
                vdsReturnValue.setReturnValue(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                        ServiceType.GLUSTER_SWIFT,
                        status));
            } else {
                // Adding one additional service for the case of stop so that save mechanism can be tested out
                Guid serverId = Guid.newGuid();
                List<GlusterServerService> serverServiceList =
                        getGlusterServerServicesByServerIdAndServiceType(serverId,
                                ServiceType.GLUSTER_SWIFT,
                                status);
                GlusterServerService srvc3 = new GlusterServerService();
                srvc3.setMessage("test-msg3");
                srvc3.setPid(10000);
                srvc3.setPort(20000);
                srvc3.setServerId(serverId);
                srvc3.setServiceId(Guid.newGuid());
                srvc3.setServiceName("srvc3");
                srvc3.setServiceType(ServiceType.GLUSTER_SWIFT);
                srvc3.setStatus(status);

                serverServiceList.add(srvc3);
                vdsReturnValue.setReturnValue(serverServiceList);
            }
        }
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.ManageGlusterService), argThat(anyServiceVDS()))).thenReturn(vdsReturnValue);
    }

    private ArgumentMatcher<VDSParametersBase> anyServiceVDS() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterServiceVDSParameters)) {
                    return false;
                }
                return true;
            }
        };
    }

    private List<GlusterServerService> getGlusterServerServicesByServerIdAndServiceType(Guid serverId,
            ServiceType serviceType, GlusterServiceStatus status) {
        List<GlusterServerService> serviceList = new ArrayList<GlusterServerService>();
        GlusterServerService srvc1 = new GlusterServerService();

        srvc1.setMessage("test-msg1");
        srvc1.setPid(10000);
        srvc1.setPort(20000);
        srvc1.setServerId(serverId);
        srvc1.setServiceId((status == GlusterServiceStatus.RUNNING) ? startedServiceId1 : stoppedServiceId1);
        srvc1.setServiceName("srvc1");
        srvc1.setServiceType(serviceType);
        srvc1.setStatus(status);
        serviceList.add(srvc1);

        GlusterServerService srvc2 = new GlusterServerService();
        srvc2.setMessage("test-msg2");
        srvc2.setPid(30000);
        srvc2.setPort(40000);
        srvc2.setServerId(serverId);
        srvc2.setServiceId((status == GlusterServiceStatus.RUNNING) ? startedServiceId2 : stoppedServiceId2);
        srvc2.setServiceName("srvc2");
        srvc2.setServiceType(serviceType);
        srvc2.setStatus(status);
        serviceList.add(srvc2);

        return serviceList;
    }

    private List<GlusterService> getGlusterServiceListByServiceType(ServiceType serviceType, GlusterServiceStatus status) {
        List<GlusterService> serviceList = new ArrayList<GlusterService>();
        GlusterService srvc1 = new GlusterService();

        srvc1.setId((status == GlusterServiceStatus.RUNNING) ? startedServiceId1 : stoppedServiceId1);
        srvc1.setServiceName("srvc1");
        srvc1.setServiceType(serviceType);
        serviceList.add(srvc1);

        GlusterService srvc2 = new GlusterService();
        srvc2.setId((status == GlusterServiceStatus.RUNNING) ? startedServiceId2 : stoppedServiceId2);
        srvc2.setServiceName("srvc2");
        srvc2.setServiceType(serviceType);
        serviceList.add(srvc2);

        return serviceList;
    }

    @Test
    public void canDoActionFails() {
        GlusterServiceParameters params = new GlusterServiceParameters();
        params.setActionType("InvalidActionType");
        cmd = spy(new ManageGlusterServiceCommand(params));
        assertFalse(cmd.canDoAction());

        params.setClusterId(null);
        params.setServerId(null);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionSucceedsWithValidActionTypes() {
        GlusterServiceParameters params = new GlusterServiceParameters();
        params.setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        cmd = spy(new ManageGlusterServiceCommand(params));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());

        params.setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd = spy(new ManageGlusterServiceCommand(params));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());

        params.setClusterId(Guid.newGuid());
        cmd = spy(new ManageGlusterServiceCommand(params));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());

        params.setServerId(Guid.newGuid());
        cmd = spy(new ManageGlusterServiceCommand(params));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());

        params.setClusterId(Guid.newGuid());
        params.setServerId(Guid.newGuid());
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(null,
                        null,
                        ServiceType.NFS,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START)));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    private void setUpMockUpForStart() {
        prepareMocks(cmd);
        when(serviceDao.getByServiceType(any(ServiceType.class))).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.STOPPED));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.STOPPED)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(Guid.class), any(ServiceType.class));

    }

    private void setUpMockUpForStop() {
        prepareMocks(cmd);
        when(serviceDao.getByServiceType(any(ServiceType.class))).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(Guid.class), any(ServiceType.class));

    }

    private void setUpMockUpForRestart() {
        prepareMocks(cmd);
        when(serviceDao.getByServiceType(any(ServiceType.class))).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(Guid.class), any(ServiceType.class));

    }

    @Test
    public void testExecuteCommandByClusterIdForStart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START)));
        setUpMockUpForStart();
        mockBackend(true, null, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STARTED);
    }

    @Test
    public void testExecuteCommandByClusterIdForStop() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP)));
        setUpMockUpForStop();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        verify(serverServiceDao, times(2)).save(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STOPPED);
    }

    @Test
    public void testExecuteCommandByClusterIdForRestart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART)));
        setUpMockUpForRestart();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_RESTARTED);
    }

    @Test
    public void testExecuteCommandByServerIdForStart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(null,
                        Guid.newGuid(),
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START)));
        setUpMockUpForStart();
        mockBackend(true, null, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STARTED);
    }

    @Test
    public void testExecuteCommandByServerIdForStop() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(null,
                        Guid.newGuid(),
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP)));
        setUpMockUpForStop();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        verify(serverServiceDao, times(1)).save(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STOPPED);
    }

    @Test
    public void testExecuteCommandByServerIdForRestart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(null,
                        Guid.newGuid(),
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART)));
        setUpMockUpForRestart();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_RESTARTED);
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForStart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_START_FAILED);
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForStop() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STOP_FAILED);
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForRestart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_RESTART_FAILED);
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForStart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_START_FAILED);
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForStop() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_STOP_FAILED);
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForRestart() {
        cmd =
                spy(new ManageGlusterServiceCommand(new GlusterServiceParameters(Guid.newGuid(),
                        null,
                        ServiceType.GLUSTER_SWIFT,
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART)));
        setUpMockUpForStart();
        mockBackend(false, VdcBllErrors.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any(GlusterServerService.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_SERVICE_RESTART_FAILED);
    }
}
