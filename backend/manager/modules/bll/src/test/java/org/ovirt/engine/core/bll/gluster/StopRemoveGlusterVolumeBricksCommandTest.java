package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class StopRemoveGlusterVolumeBricksCommandTest extends AbstractRemoveGlusterVolumeBricksCommandTest {
    private final Guid volumeWithRemoveBricksTaskCompleted = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private StopRemoveGlusterVolumeBricksCommand cmd =
            new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(), null);

    private void setVolumeId(Guid volumeId) {
        cmd.setGlusterVolumeId(volumeId);
        cmd.getParameters().setBricks(getBricks(volumeId));
    }

    @Before
    public void prepareMocks() {
        GlusterBrickValidator brickValidator = spy(cmd.getBrickValidator());
        doReturn(brickDao).when(brickValidator).getGlusterBrickDao();
        doReturn(brickValidator).when(cmd).getBrickValidator();
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVolumeWithRemoveBricksTask(volumeWithRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithRemoveBricksTask);
        doReturn(getBricks(volumeWithoutRemoveBricksTask)).when(brickDao)
                .getGlusterVolumeBricksByTaskId(any(Guid.class));
        doReturn(getVolumeWithRemoveBricksTaskCompleted(volumeWithRemoveBricksTaskCompleted)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskCompleted);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getVolumeWithoutRemoveBricksTask(volumeWithoutRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithoutRemoveBricksTask);
        doReturn(cluster).when(cmd).getCluster();
        doReturn(vdsBrokerFrontend).when(cmd).getVdsBroker();
    }

    private Object getVolumeWithRemoveBricksTaskCompleted(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRemoveBricksTask(volumeId);
        volume.getAsyncTask().setStatus(JobExecutionStatus.FINISHED);
        return volume;
    }

    @Override
    protected GlusterVolumeEntity getVolumeWithRemoveBricksTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setStatus(JobExecutionStatus.STARTED);
        asyncTask.setType(GlusterTaskType.REMOVE_BRICK);
        volume.setAsyncTask(asyncTask);
        return volume;
    }

    private void mockBackend(boolean succeeded, EngineError errorCode) {
        doReturn(backend).when(cmd).getBackend();
        doNothing().when(cmd).endStepJobAborted(any(String.class));
        doNothing().when(cmd).releaseVolumeLock();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        } else {
            vdsReturnValue.setReturnValue(new GlusterVolumeTaskStatusEntity());
        }

        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.StopRemoveGlusterVolumeBricks),
                any(GlusterVolumeVDSParameters.class))).thenReturn(vdsReturnValue);
    }

    @Test
    public void testExecuteCommand() {
        setVolumeId(volumeWithRemoveBricksTask);
        mockBackend(true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, times(1)).endStepJobAborted(any(String.class));
        verify(cmd, times(1)).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setVolumeId(volumeWithRemoveBricksTask);
        mockBackend(false, EngineError.GlusterVolumeRemoveBricksStopFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, never()).endStepJobAborted(any(String.class));
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTask() {
        setVolumeId(volumeWithRemoveBricksTask);
        assertTrue(cmd.validate());
    }

    // This happens in retain bricks scenario, where user can call stop remove brick after migrating the data
    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTaskCompleted() {
        setVolumeId(volumeWithRemoveBricksTaskCompleted);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        setVolumeId(volumeWithoutAsyncTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutRemoveBricksTask() {
        setVolumeId(volumeWithoutRemoveBricksTask);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd.getParameters().setBricks(getBricks(volumeWithRemoveBricksTaskCompleted));
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithEmptyBricksList() {
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
