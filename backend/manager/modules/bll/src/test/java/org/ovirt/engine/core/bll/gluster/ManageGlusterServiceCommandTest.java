package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ManageGlusterServiceCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    ManageGlusterServiceCommand cmd = new ManageGlusterServiceCommand(new GlusterServiceParameters(), null);

    @Mock
    GlusterServerServiceDao serverServiceDao;
    @Mock
    GlusterServiceDao serviceDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    GlusterUtil glusterUtils;

    private final Guid startedServiceId1 = Guid.newGuid();
    private final Guid startedServiceId2 = Guid.newGuid();
    private final Guid stoppedServiceId1 = Guid.newGuid();
    private final Guid stoppedServiceId2 = Guid.newGuid();

    private void prepareMocks() {
        doReturn(getUpServers()).when(glusterUtils).getAllUpServers(any());
    }

    private List<VDS> getUpServers() {
        final VDS vds1 = new VDS();
        vds1.setId(Guid.newGuid());
        vds1.setVdsName("gfs1");
        vds1.setClusterId(Guid.newGuid());
        vds1.setStatus(VDSStatus.Up);
        final VDS vds2 = new VDS();
        vds2.setId(Guid.newGuid());
        vds2.setVdsName("gfs2");
        vds2.setClusterId(Guid.newGuid());
        vds2.setStatus(VDSStatus.Up);

        return Arrays.asList(vds1, vds2);
    }

    private void mockBackend(boolean succeeded, EngineError errorCode, GlusterServiceStatus status) {
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
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.ManageGlusterService), any())).thenReturn(vdsReturnValue);
    }

    private List<GlusterServerService> getGlusterServerServicesByServerIdAndServiceType(Guid serverId,
            ServiceType serviceType, GlusterServiceStatus status) {
        List<GlusterServerService> serviceList = new ArrayList<>();
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
        List<GlusterService> serviceList = new ArrayList<>();
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
    public void validateFails() {
        cmd.getParameters().setActionType("InvalidActionType");
        assertFalse(cmd.validate());
    }

    @Test
    public void validateSucceedsWithStartActionType() {
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        prepareMocks();
        assertFalse(cmd.validate());
    }

    @Test
    public void validateSucceedsWithStopActionType() {
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        prepareMocks();
        assertFalse(cmd.validate());
    }

    @Test
    public void validateSucceedsWithStopActionTypeAndClusterId() {
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        prepareMocks();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateSucceedsWithStopActionTypeAndClusterIdAndServerId() {
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        cmd.setVdsId(cmd.getParameters().getServerId());
        prepareMocks();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd.getParameters().setServiceType(ServiceType.NFS);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        prepareMocks();
        assertFalse(cmd.validate());
    }

    private void setUpMockUpForStart() {
        prepareMocks();
        when(serviceDao.getByServiceType(any())).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.STOPPED));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.STOPPED)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(), any());

    }

    private void setUpMockUpForStop() {
        prepareMocks();
        when(serviceDao.getByServiceType(any())).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(), any());

    }

    private void setUpMockUpForRestart() {
        prepareMocks();
        when(serviceDao.getByServiceType(any())).thenReturn(getGlusterServiceListByServiceType(ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING));
        doReturn(getGlusterServerServicesByServerIdAndServiceType(Guid.newGuid(),
                ServiceType.GLUSTER_SWIFT,
                GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerIdAndServiceType(any(), any());

    }

    @Test
    public void testExecuteCommandByClusterIdForStart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForStart();
        mockBackend(true, null, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STARTED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandByClusterIdForStop() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForStop();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any());
        verify(serverServiceDao, times(2)).save(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STOPPED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandByClusterIdForRestart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForRestart();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(4)).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_RESTARTED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandByServerIdForStart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForStart();
        mockBackend(true, null, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STARTED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandByServerIdForStop() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForStop();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any());
        verify(serverServiceDao, times(1)).save(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STOPPED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandByServerIdForRestart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForRestart();
        mockBackend(true, null, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, times(2)).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_RESTARTED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForStart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_START_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForStop() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STOP_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithClusterIdForRestart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART);
        cmd.getParameters().setClusterId(Guid.newGuid());
        cmd.setClusterId(cmd.getParameters().getClusterId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_RESTART_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForStart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.STOPPED);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_START_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForStop() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_STOP_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void testExecuteCommandFailsWithServerIdForRestart() {
        cmd.getParameters().setServiceType(ServiceType.GLUSTER_SWIFT);
        cmd.getParameters().setActionType(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART);
        cmd.getParameters().setServerId(Guid.newGuid());
        cmd.setVdsId(cmd.getParameters().getServerId());
        setUpMockUpForStart();
        mockBackend(false, EngineError.GlusterServicesActionFailed, GlusterServiceStatus.RUNNING);
        cmd.executeCommand();
        verify(serverServiceDao, never()).updateByServerIdAndServiceType(any());
        assertEquals(AuditLogType.GLUSTER_SERVICE_RESTART_FAILED, cmd.getAuditLogTypeValue());
    }
}
