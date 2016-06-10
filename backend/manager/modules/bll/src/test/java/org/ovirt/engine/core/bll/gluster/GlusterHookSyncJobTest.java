package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;

@RunWith(MockitoJUnitRunner.class)
public class GlusterHookSyncJobTest {
    private static final Guid[] SERVER_GUIDS = {new Guid("11111111-1111-1111-1111-111111111111"),
        new Guid("22222222-2222-2222-2222-222222222222"),
        new Guid("33333333-3333-3333-3333-333333333333")};
    private static final Guid[] CLUSTER_GUIDS = {new Guid("CC111111-1111-1111-1111-111111111111"),
        new Guid("CC222222-2222-2222-2222-222222222222")};

    private static final Guid[] EXISTING_HOOK_IDS = {new Guid("AAAAAAAA-1111-1111-1111-111111111111"),
        new Guid("AAAAAAAA-2222-2222-2222-222222222222"),
        new Guid("AAAAAAAA-3333-3333-3333-333333333333")};

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private GlusterHooksDao hooksDao;

    @Mock
    private ClusterDao clusterDao;

    private GlusterHookSyncJob hookSyncJob;
    private GlusterAuditLogUtil logUtil;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10));

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private void mockDaos() {

        doReturn(clusterDao).when(hookSyncJob).getClusterDao();
        doReturn(hooksDao).when(hookSyncJob).getHooksDao();

        List<Cluster> clusters = new ArrayList<>();
        clusters.add(createCluster(0));
        clusters.add(createCluster(1)); //to check for empty cluster

        doReturn(clusters).when(clusterDao).getAll();
    }

    private void initMocks() {

        hookSyncJob = Mockito.spy(GlusterHookSyncJob.getInstance());
        logUtil = Mockito.spy(GlusterAuditLogUtil.getInstance());
        hookSyncJob.setLogUtil(logUtil);
        doReturn(glusterUtil).when(hookSyncJob).getGlusterUtil();
        doReturn(getServers()).when(glusterUtil).getAllUpServers(CLUSTER_GUIDS[0]);
        doReturn(Collections.emptyList()).when(glusterUtil).getAllUpServers(CLUSTER_GUIDS[1]);
        doNothing().when(logUtil).logAuditMessage(any(Guid.class),
                any(GlusterVolumeEntity.class),
                any(VDS.class),
                any(AuditLogType.class),
                anyMapOf(String.class, String.class));
        mockDaos();
    }

    private Object getHooksListVDSReturnVal(int count) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getHooksList(count, false));
        return vdsRetValue;
    }

    private Object getHooksListVDSReturnVal(List<GlusterHookEntity> list) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(list);
        return vdsRetValue;
    }

    private Object getHookContentVDSReturnVal() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue("CONTENT");
        return vdsRetValue;
    }

    private List<GlusterHookEntity> getHooksList(int listCount, boolean setIds) {
        List<GlusterHookEntity> hookList = new ArrayList<>();
        for (int i=0; i< listCount; i++) {
            hookList.add(getHook( i , setIds));
        }
        return hookList;
    }

    private GlusterHookEntity getHook(int index, boolean setId) {
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setChecksum("234230--090934");
        hook.setContentType(GlusterHookContentType.TEXT);
        hook.setStatus(GlusterHookStatus.ENABLED);
        hook.setGlusterCommand("create");
        hook.setStage("PRE");
        hook.setName("hook-"+index);
        if (setId) {
            hook.setId(EXISTING_HOOK_IDS[index]);
            hook.setClusterId(CLUSTER_GUIDS[0]);
        }
        return hook;
    }

    private ArgumentMatcher<VDSParametersBase> vdsServer1() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof VdsIdVDSCommandParametersBase)) {
                    return false;
                }
                return((VdsIdVDSCommandParametersBase) argument).getVdsId().equals(SERVER_GUIDS[0]);
            }
        };
    }

    private ArgumentMatcher<VDSParametersBase> vdsServer2() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof VdsIdVDSCommandParametersBase)) {
                    return false;
                }
                return((VdsIdVDSCommandParametersBase) argument).getVdsId().equals(SERVER_GUIDS[1]);
            }
        };
    }
    private List<VDS> getServers() {
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(createServer(SERVER_GUIDS[0], "HOST-0"));
        vdsList.add(createServer(SERVER_GUIDS[1], "HOST-1"));
        return vdsList;
    }

    private VDS createServer(Guid serverId, String hostname) {
        VDS vds = new VDS();
        vds.setId(serverId);
        vds.setHostName(hostname);
        vds.setStatus(VDSStatus.Up);
        return vds;
    }

    private Cluster createCluster(int index) {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_GUIDS[index]);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        return cluster;
    }

    @Test
    public void syncHooksWhenDBInSyncWithServers() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(0)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(0)).saveOrUpdateGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(0)).updateGlusterHookConflictStatus(any(Guid.class), any(Integer.class));
    }

    @Test
    public void syncHooksWhenNewHooksFound() {
        initMocks();
        doReturn(getHooksList(1, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));

        doReturn(getHookContentVDSReturnVal()).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GetGlusterHookContent),
                any(GlusterHookVDSParameters.class));

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(2)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(2)).updateGlusterHookContent(any(Guid.class), any(String.class), any(String.class));
        Mockito.verify(hooksDao, times(0)).saveOrUpdateGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(0)).updateGlusterHookConflictStatus(any(Guid.class), any(Integer.class));
    }

    @Test
    public void syncHooksWhenHookMissingInAllServers() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(0)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(2)).saveOrUpdateGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(2)).updateGlusterHookConflictStatus(any(Guid.class), any(Integer.class));
    }

    @Test
    public void syncHooksWhenHookMissingInOneServer() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(0)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(1)).saveOrUpdateGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(1)).updateGlusterHookConflictStatus(any(Guid.class), any(Integer.class));
    }

    @Test
    public void syncHooksWhenHookContentConflictInOneServer() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        List<GlusterHookEntity> listHooks = getHooksList(3, false);
        listHooks.get(1).setChecksum("NEWCHECKSUM");
        doReturn(getHooksListVDSReturnVal(listHooks)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(0)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(1)).saveGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[1], GlusterHookConflictFlags.CONTENT_CONFLICT.getValue());
    }

    @Test
    public void syncHooksWhenHookMissingAndContentConflict() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer1()));
        List<GlusterHookEntity> listHooks = getHooksList(3, false);
        listHooks.get(1).setChecksum("NEWCHECKSUM");
        doReturn(getHooksListVDSReturnVal(listHooks)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServer2()));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        Mockito.verify(hooksDao, times(0)).save(any(GlusterHookEntity.class));
        Mockito.verify(hooksDao, times(1)).saveOrUpdateGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(1)).saveGlusterServerHook(any(GlusterServerHook.class));
        Mockito.verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[1], GlusterHookConflictFlags.CONTENT_CONFLICT.getValue());
        Mockito.verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[2], GlusterHookConflictFlags.MISSING_HOOK.getValue());

    }

}
