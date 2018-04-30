package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class StopRebalanceGlusterVolumeCommandTest extends BaseCommandTest {
    @Mock
    GlusterVolumeDao volumeDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;
    @Spy
    private GlusterTaskUtils glusterTaskUtils;

    private final Guid volumeWithRebalanceTask = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private final Guid volumeWithRebalanceTaskCompleted = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeWithoutAsyncTask = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid volumeWithoutRebalanceTask = new Guid("000000000000-0000-0000-0000-00000004");
    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Mock
    protected Cluster cluster;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private StopRebalanceGlusterVolumeCommand cmd =
            new StopRebalanceGlusterVolumeCommand(new GlusterVolumeRebalanceParameters(), null);

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVolumeWithRebalanceTask(volumeWithRebalanceTask)).when(volumeDao).getById(volumeWithRebalanceTask);
        doReturn(getVolumeWithRebalanceTaskCompleted(volumeWithRebalanceTaskCompleted)).when(volumeDao)
                .getById(volumeWithRebalanceTaskCompleted);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getvolumeWithoutRebalanceTask(volumeWithoutRebalanceTask)).when(volumeDao)
                .getById(volumeWithoutRebalanceTask);
        doReturn(cluster).when(cmd).getCluster();
    }

    private Object getvolumeWithoutRebalanceTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRebalanceTask(volumeId);
        volume.getAsyncTask().setType(null);
        return volume;
    }

    private Object getVolumeWithRebalanceTaskCompleted(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRebalanceTask(volumeId);
        volume.getAsyncTask().setStatus(JobExecutionStatus.FINISHED);
        return volume;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterVolumeEntity getVolumeWithRebalanceTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setStatus(JobExecutionStatus.STARTED);
        asyncTask.setType(GlusterTaskType.REBALANCE);
        volume.setAsyncTask(asyncTask);
        return volume;
    }

    private GlusterVolumeEntity getVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setStatus(GlusterStatus.UP);
        volumeEntity.setBricks(getBricks(id, 2));
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setClusterId(CLUSTER_ID);
        return volumeEntity;
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int n) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < n; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setBrickDirectory("/tmp/test-vol" + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    private void mockBackend(boolean succeeded,
            JobExecutionStatus rebalanceStopStatus,
            boolean isRebalancegTaskCompleted,
            EngineError errorCode) {
        doReturn("TestVDS").when(cmd).getClusterName();
        doReturn("TestVolume").when(cmd).getGlusterVolumeName();
        doNothing().when(cmd).endStepJob(eq(rebalanceStopStatus), any(), eq(isRebalancegTaskCompleted));

        doNothing().when(cmd).releaseVolumeLock();
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        GlusterVolumeTaskStatusEntity rebalanceStatusEntity = new GlusterVolumeTaskStatusEntity();
        rebalanceStatusEntity.getStatusSummary().setStatus(rebalanceStopStatus);
        vdsReturnValue.setReturnValue(rebalanceStatusEntity);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.StopRebalanceGlusterVolume), any())).thenReturn(vdsReturnValue);
    }

    @Test
    public void executeCommand() {
        cmd.setGlusterVolumeId(volumeWithRebalanceTask);
        mockBackend(true, JobExecutionStatus.ABORTED, false, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd).endStepJob(eq(JobExecutionStatus.ABORTED), any(), eq(false));
        verify(cmd).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWithRebalanceCompleteInNode() {
        cmd.setGlusterVolumeId(volumeWithRebalanceTask);
        mockBackend(true, JobExecutionStatus.FINISHED, true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd).endStepJob(eq(JobExecutionStatus.FINISHED), any(), eq(true));
        verify(cmd).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd.setGlusterVolumeId(volumeWithRebalanceTask);
        mockBackend(false, JobExecutionStatus.FAILED, false, EngineError.GlusterVolumeRebalanceStopFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, never()).endStepJob(any(), any(), anyBoolean());
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceedsOnVolumeWithRebalanceTask() {
        cmd.setGlusterVolumeId(volumeWithRebalanceTask);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        cmd.setGlusterVolumeId(volumeWithoutAsyncTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutRebalanceTask() {
        cmd.setGlusterVolumeId(volumeWithoutRebalanceTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailesOnVolumeWithRebalanceTaskCompleted() {
        cmd.setGlusterVolumeId(volumeWithRebalanceTaskCompleted);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }
}
