package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
public class CommitRemoveGlusterVolumeBricksCommandTest extends AbstractRemoveGlusterVolumeBricksCommandTest {
    @Mock
    protected GlusterDBUtils dbUtils;

    private final Guid volumeWithRemoveBricksTaskNotFinished = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeWithRemoveBricksTaskNull = new Guid("000000000000-0000-0000-0000-00000005");

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private CommitRemoveGlusterVolumeBricksCommand cmd =
            new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(), null);

    private void setVolume(Guid volumeId) {
        cmd.setGlusterVolumeId(volumeId);
        cmd.getParameters().setBricks(getBricks(volumeId));
    }

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVolumeWithRemoveBricksTask(volumeWithRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithRemoveBricksTask);
        doReturn(getBricks(volumeWithoutRemoveBricksTask)).when(brickDao).getGlusterVolumeBricksByTaskId(any());
        doReturn(getVolumeWithRemoveBricksTaskNotFinished(volumeWithRemoveBricksTaskNotFinished)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskNotFinished);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getVolumeWithoutRemoveBricksTask(volumeWithoutRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithoutRemoveBricksTask);
        doReturn(getVolumeWithRemoveBricksTaskNull(volumeWithRemoveBricksTaskNull)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskNull);
    }

    private Object getVolumeWithRemoveBricksTaskNull(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setAsyncTask(null);
        return volume;
    }

    private Object getVolumeWithRemoveBricksTaskNotFinished(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRemoveBricksTask(volumeId);
        volume.getAsyncTask().setStatus(JobExecutionStatus.STARTED);
        return volume;
    }

    @Override
    protected GlusterVolumeEntity getVolumeWithRemoveBricksTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setStatus(JobExecutionStatus.FINISHED);
        asyncTask.setType(GlusterTaskType.REMOVE_BRICK);
        volume.setAsyncTask(asyncTask);
        return volume;
    }

    @SuppressWarnings("unchecked")
    private void mockBackend(boolean succeeded, EngineError errorCode) {
        doNothing().when(cmd).endStepJobCommitted();
        doNothing().when(cmd).releaseVolumeLock();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }

        when(vdsBrokerFrontend.runVdsCommand(
                eq(VDSCommandType.CommitRemoveGlusterVolumeBricks), any())).thenReturn(vdsReturnValue);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteCommand() {
        setVolume(volumeWithRemoveBricksTask);
        mockBackend(true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, times(1)).endStepJobCommitted();
        verify(cmd, times(1)).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setVolume(volumeWithRemoveBricksTask);
        mockBackend(false, EngineError.GlusterVolumeRemoveBricksCommitFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, never()).endStepJobCommitted();
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTask() {
        setVolume(volumeWithRemoveBricksTask);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        setVolume(volumeWithoutAsyncTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutRemoveBricksTask() {
        setVolume(volumeWithoutRemoveBricksTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailesOnVolumeWithRemoveBricksTaskNotFinished() {
        setVolume(volumeWithRemoveBricksTaskNotFinished);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        setVolume(volumeWithRemoveBricksTaskNull);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithEmptyBricksList() {
        cmd.setGlusterVolumeId(volumeWithoutAsyncTask);
        cmd.getParameters().setBricks(Collections.emptyList());
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithInvalidNoOfBricks() {
        cmd.setGlusterVolumeId(volumeWithRemoveBricksTask);
        cmd.getParameters().setBricks(getInvalidNoOfBricks(volumeWithRemoveBricksTask));
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithInvalidBricks() {
        cmd.setGlusterVolumeId(volumeWithRemoveBricksTask);
        cmd.getParameters().setBricks(getInvalidBricks(volumeWithRemoveBricksTask));
        assertFalse(cmd.validate());
    }
}
