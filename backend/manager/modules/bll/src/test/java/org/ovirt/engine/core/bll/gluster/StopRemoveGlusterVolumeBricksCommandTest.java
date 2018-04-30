package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVolumeWithRemoveBricksTask(volumeWithRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithRemoveBricksTask);
        doReturn(getBricks(volumeWithoutRemoveBricksTask)).when(brickDao)
                .getGlusterVolumeBricksByTaskId(any());
        doReturn(getVolumeWithRemoveBricksTaskCompleted(volumeWithRemoveBricksTaskCompleted)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskCompleted);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getVolumeWithoutRemoveBricksTask(volumeWithoutRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithoutRemoveBricksTask);
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
        doNothing().when(cmd).endStepJobAborted(any());
        doNothing().when(cmd).releaseVolumeLock();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        } else {
            vdsReturnValue.setReturnValue(new GlusterVolumeTaskStatusEntity());
        }

        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.StopRemoveGlusterVolumeBricks),
                any())).thenReturn(vdsReturnValue);
    }

    @Test
    public void testExecuteCommand() {
        setVolumeId(volumeWithRemoveBricksTask);
        mockBackend(true, null);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        cmd.executeCommand();

        verify(cmd, times(1)).endStepJobAborted(any());
        verify(cmd, times(1)).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setVolumeId(volumeWithRemoveBricksTask);
        mockBackend(false, EngineError.GlusterVolumeRemoveBricksStopFailed);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        cmd.executeCommand();

        verify(cmd, never()).endStepJobAborted(any());
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTask() {
        setVolumeId(volumeWithRemoveBricksTask);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    // This happens in retain bricks scenario, where user can call stop remove brick after migrating the data
    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTaskCompleted() {
        setVolumeId(volumeWithRemoveBricksTaskCompleted);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        setVolumeId(volumeWithoutAsyncTask);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID_TASK_TYPE);
    }

    @Test
    public void validateFailsOnVolumeWithoutRemoveBricksTask() {
        setVolumeId(volumeWithoutRemoveBricksTask);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID_TASK_TYPE);
    }

    @Test
    public void validateFailsOnNull() {
        cmd.getParameters().setBricks(getBricks(volumeWithRemoveBricksTaskCompleted));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
    }

    @Test
    public void validateFailsWithEmptyBricksList() {
        cmd.getParameters().setBricks(Collections.emptyList());
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
    }

    @Test
    public void validateFailsWithInvalidNoOfBricks() {
        cmd.setGlusterVolumeId(volumeWithRemoveBricksTask);
        cmd.getParameters().setBricks(getInvalidNoOfBricks(volumeWithRemoveBricksTask));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_PARAMS_INVALID);
    }

    @Test
    public void validateFailsWithInvalidBricks() {
        cmd.setGlusterVolumeId(volumeWithRemoveBricksTask);
        cmd.getParameters().setBricks(getInvalidBricks(volumeWithRemoveBricksTask));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_PARAMS_INVALID);
    }
}
