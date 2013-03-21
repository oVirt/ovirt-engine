package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class EnableGlusterHookCommandTest {
    private static final Guid[] GUIDS = {Guid.createGuidFromString("afce7a39-8e8c-4819-ba9c-796d316592e6"),
                                         Guid.createGuidFromString("afce7a39-8e8c-4819-ba9c-796d316592e7"),
                                         Guid.createGuidFromString("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3"),
                                         Guid.createGuidFromString("2001751e-549b-4e7a-aff6-32d36856c125")};
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid HOOK_ID = Guid.createGuidFromString("d2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    /**
     * The command under test.
     */
    private EnableGlusterHookCommand cmd;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize,10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20));


    @Mock
    private GlusterHooksDao hooksDao;

    @Mock
    private VdsGroupDAO vdsGroupDao;

    @Mock
    private BackendInternal backend;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private ClusterUtils clusterUtils;


    public void setupMocks(boolean hookFound) {

        when(clusterUtils.getAllUpServers(CLUSTER_ID)).thenReturn(getGlusterServers());
        doReturn(clusterUtils).when(cmd).getClusterUtils();
        if (hookFound) {
            when(hooksDao.getById(HOOK_ID)).thenReturn(getHookEntity());
            when(hooksDao.getById(HOOK_ID,true)).thenReturn(getHookEntity());
        }
        doReturn(hooksDao).when(cmd).getGlusterHooksDao();
        when(vdsGroupDao.get(CLUSTER_ID)).thenReturn(getVdsGroup());
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDAO();
        doReturn(getGlusterServers().get(0)).when(cmd).getUpServer();
    }

    private void mockBackend(boolean succeeded) {
        when(cmd.getBackend()).thenReturn(backend);

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(succeeded);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(VdcBllErrors.GlusterHookEnableFailed, ""));
        }
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class))).thenReturn(vdsReturnValue);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
    }

    @Test
    public void executeCommand() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(true);
        mockBackend(true);
        cmd.executeCommand();
        verify(cmd, atLeast(1)).addServerHookInDb(any(GlusterServerHook.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE);
    }
    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(true);
        mockBackend(false);
        cmd.executeCommand();
        verify(cmd, never()).addServerHookInDb(any(GlusterServerHook.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);
    }

    private GlusterHookEntity getHookEntity() {
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setClusterId(CLUSTER_ID);
        hook.setId(HOOK_ID);
        hook.setServerHooks(getGlusterServerHooks());
        return hook;
    }

    private List<VDS> getGlusterServers() {
        List<VDS> servers = new ArrayList<VDS>();
        servers.add(getServer(GUIDS[0], "gfs1", CLUSTER_ID));
        servers.add(getServer(GUIDS[1], "gfs2", CLUSTER_ID));
        servers.add(getServer(GUIDS[2], "gfs3", CLUSTER_ID));
        servers.add(getServer(GUIDS[3], "gfs4", CLUSTER_ID));
        return servers;
    }

    private List<GlusterServerHook> getGlusterServerHooks() {
        List<GlusterServerHook> serverHooks = new ArrayList<GlusterServerHook>();
        serverHooks.add(getGlusterServerHook(0));
        serverHooks.add(getGlusterServerHook(1));
        serverHooks.add(getGlusterServerHook(2));
        return serverHooks;
    }

    private VDS getServer(Guid id, String name, Guid clusterId) {
        VDS server =  new VDS();
        server.setId(id);
        server.setVdsName(name);
        server.setStatus(VDSStatus.Up);
        server.setVdsGroupId(clusterId);
        return server;
    }

    private VDSGroup getVdsGroup() {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(CLUSTER_ID);
        cluster.setname("TestCluster");
        return cluster;
    }


    private GlusterServerHook getGlusterServerHook(int index) {
        GlusterServerHook serverHook = new GlusterServerHook();
        serverHook.setServerId(GUIDS[index]);
        return serverHook;
    }

     @Test
    public void canDoActionSucceeds() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(true);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullCluster() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(null, HOOK_ID)));
        setupMocks(true);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

    @Test
    public void canDoActionFailsOnNullHookId() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, null)));
        setupMocks(true);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void canDoActionFailsOnNoHook() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(false);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

}
