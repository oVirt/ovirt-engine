package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;


public abstract class GlusterHookCommandTest<T extends GlusterHookCommandBase<? extends GlusterHookParameters>>
        extends BaseCommandTest {

    protected static final Guid[] GUIDS = {new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"),
                                             new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"),
                                             new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3"),
                                             new Guid("2001751e-549b-4e7a-aff6-32d36856c125")};
    protected static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    protected static final Guid HOOK_ID = new Guid("d2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    @Mock
    protected GlusterHooksDao hooksDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private GlusterUtil glusterUtils;
    @Mock
    private ClusterUtils clusterUtils;

    protected abstract T createCommand();

    @Spy
    @InjectMocks
    protected T cmd = createCommand();

    public void setupMocks() {
        setupMocks(true);
    }

    public void setupMocks(boolean hookFound) {
        setupMocks(hookFound, getHookEntity());
    }

    public void setupMocks(boolean hookFound, GlusterHookEntity hookEntity) {
        when(glusterUtils.getAllUpServers(CLUSTER_ID)).thenReturn(getGlusterServers());
        if (hookFound) {
            when(hooksDao.getById(HOOK_ID)).thenReturn(hookEntity);
            when(hooksDao.getById(HOOK_ID, true)).thenReturn(hookEntity);
        }
        when(clusterDao.get(CLUSTER_ID)).thenReturn(getCluster());
        doReturn(getGlusterServers().get(0)).when(cmd).getUpServer();
    }

    protected void mockBackendStatusChange(boolean succeeded) {
        mockBackendStatusChange(succeeded, EngineError.GlusterHookEnableFailed);
    }

    protected void mockBackendStatusChange(boolean succeeded, EngineError errorCode) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(succeeded);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(any(), any())).thenReturn(vdsReturnValue);
    }



    protected GlusterHookEntity getHookEntity() {
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setClusterId(CLUSTER_ID);
        hook.setId(HOOK_ID);
        hook.setServerHooks(getGlusterServerHooks());
        return hook;
    }

    private List<VDS> getGlusterServers() {
        List<VDS> servers = new ArrayList<>();
        servers.add(getServer(GUIDS[0], "gfs1", CLUSTER_ID));
        servers.add(getServer(GUIDS[1], "gfs2", CLUSTER_ID));
        servers.add(getServer(GUIDS[2], "gfs3", CLUSTER_ID));
        servers.add(getServer(GUIDS[3], "gfs4", CLUSTER_ID));
        return servers;
    }

    private List<GlusterServerHook> getGlusterServerHooks() {
        List<GlusterServerHook> serverHooks = new ArrayList<>();
        serverHooks.add(getGlusterServerHook(0, GlusterHookStatus.ENABLED));
        serverHooks.add(getGlusterServerHook(1, GlusterHookStatus.ENABLED));
        serverHooks.add(getGlusterServerHook(2, GlusterHookStatus.ENABLED));
        return serverHooks;
    }

    protected VDS getServer(Guid id, String name, Guid clusterId) {
        return getServer(id, name, clusterId, VDSStatus.Up);
    }

    protected VDS getServer(Guid id, String name, Guid clusterId, VDSStatus status) {
        VDS server =  new VDS();
        server.setId(id);
        server.setVdsName(name);
        server.setStatus(status);
        server.setClusterId(clusterId);
        return server;
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setName("TestCluster");
        return cluster;
    }


    protected GlusterServerHook getGlusterServerHook(int index, GlusterHookStatus status) {
        GlusterServerHook serverHook = new GlusterServerHook();
        serverHook.setServerId(GUIDS[index]);
        serverHook.setStatus(status);
        serverHook.setChecksum("CHECKSUM");
        return serverHook;
    }
}
