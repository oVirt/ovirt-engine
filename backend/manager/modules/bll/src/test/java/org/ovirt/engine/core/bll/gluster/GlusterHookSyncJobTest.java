package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;

@ExtendWith({MockitoExtension.class, ExecutorServiceExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @InjectMocks
    @Spy
    private GlusterHookSyncJob hookSyncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    private void mockDaos() {
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(createCluster(0));
        clusters.add(createCluster(1)); //to check for empty cluster

        doReturn(clusters).when(clusterDao).getAll();
        doReturn(clusters.get(0)).when(clusterDao).get(any());
    }

    private void initMocks() {
        doReturn(getServers()).when(glusterUtil).getAllUpServers(CLUSTER_GUIDS[0]);
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

    private ArgumentMatcher<VdsIdVDSCommandParametersBase> vdsServerWithGuid(int index) {
        return argument -> argument.getVdsId().equals(SERVER_GUIDS[index]);
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
                argThat(vdsServerWithGuid(0)));
        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(0)).save(any());
        verify(hooksDao, times(0)).saveOrUpdateGlusterServerHook(any());
        verify(hooksDao, times(0)).updateGlusterHookConflictStatus(any(), any());
    }

    @Test
    public void syncHooksWhenNewHooksFound() {
        initMocks();
        doReturn(getHooksList(1, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(0)));
        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));

        doReturn(getHookContentVDSReturnVal()).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GetGlusterHookContent),
                any());

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(2)).save(any());
        verify(hooksDao, times(2)).updateGlusterHookContent(any(), any(), any());
        verify(hooksDao, times(0)).saveOrUpdateGlusterServerHook(any());
        verify(hooksDao, times(0)).updateGlusterHookConflictStatus(any(), any());
    }

    @Test
    public void syncHooksWhenHookMissingInAllServers() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(0)));
        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(0)).save(any());
        verify(hooksDao, times(2)).saveOrUpdateGlusterServerHook(any());
        verify(hooksDao, times(2)).updateGlusterHookConflictStatus(any(), any());
    }

    @Test
    public void syncHooksWhenHookMissingInOneServer() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(0)));
        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(0)).save(any());
        verify(hooksDao, times(1)).saveOrUpdateGlusterServerHook(any());
        verify(hooksDao, times(1)).updateGlusterHookConflictStatus(any(), any());
    }

    @Test
    public void syncHooksWhenHookContentConflictInOneServer() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(3)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(0)));
        List<GlusterHookEntity> listHooks = getHooksList(3, false);
        listHooks.get(1).setChecksum("NEWCHECKSUM");
        doReturn(getHooksListVDSReturnVal(listHooks)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(0)).save(any());
        verify(hooksDao, times(1)).saveGlusterServerHook(any());
        verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[1], GlusterHookConflictFlags.CONTENT_CONFLICT.getValue());
    }

    @Test
    public void syncHooksWhenHookMissingAndContentConflict() {
        initMocks();
        doReturn(getHooksList(3, true)).when(hooksDao).getByClusterId(CLUSTER_GUIDS[0]);

        doReturn(getHooksListVDSReturnVal(2)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(0)));
        List<GlusterHookEntity> listHooks = getHooksList(3, false);
        listHooks.get(1).setChecksum("NEWCHECKSUM");
        doReturn(getHooksListVDSReturnVal(listHooks)).when(hookSyncJob).runVdsCommand(eq(VDSCommandType.GlusterHooksList),
                argThat(vdsServerWithGuid(1)));
        doReturn(getHook(2, true)).when(hooksDao).getById(EXISTING_HOOK_IDS[2]);

        hookSyncJob.refreshHooks();
        verify(hooksDao, times(0)).save(any());
        verify(hooksDao, times(1)).saveOrUpdateGlusterServerHook(any());
        verify(hooksDao, times(1)).saveGlusterServerHook(any());
        verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[1], GlusterHookConflictFlags.CONTENT_CONFLICT.getValue());
        verify(hooksDao, times(1)).updateGlusterHookConflictStatus(EXISTING_HOOK_IDS[2], GlusterHookConflictFlags.MISSING_HOOK.getValue());

    }

}
