package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

@RunWith(MockitoJUnitRunner.class)
public class CommitRemoveGlusterVolumeBricksCommandTest extends AbstractRemoveGlusterVolumeBricksCommandTest {
    @Mock
    protected GlusterDBUtils dbUtils;

    private final Guid volumeWithRemoveBricksTaskNotFinished = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeWithRemoveBricksTaskNull = new Guid("000000000000-0000-0000-0000-00000005");

    /**
     * The command under test.
     */
    private CommitRemoveGlusterVolumeBricksCommand cmd;

    private void prepareMocks(CommitRemoveGlusterVolumeBricksCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        GlusterBrickValidator brickValidator = spy(command.getBrickValidator());
        doReturn(brickValidator).when(command).getBrickValidator();
        doReturn(brickDao).when(brickValidator).getGlusterBrickDao();
        doReturn(dbUtils).when(command).getDbUtils();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getVolumeWithRemoveBricksTask(volumeWithRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithRemoveBricksTask);
        doReturn(getBricks(volumeWithoutRemoveBricksTask)).when(brickDao).getGlusterVolumeBricksByTaskId(any(Guid.class));
        doReturn(getVolumeWithRemoveBricksTaskNotFinished(volumeWithRemoveBricksTaskNotFinished)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskNotFinished);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getVolumeWithoutRemoveBricksTask(volumeWithoutRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithoutRemoveBricksTask);
        doReturn(getVolumeWithRemoveBricksTaskNull(volumeWithRemoveBricksTaskNull)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskNull);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(cluster).when(command).getCluster();
        doReturn(vdsBrokerFrontend).when(command).getVdsBroker();
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
        doReturn(backend).when(cmd).getBackend();
        doNothing().when(cmd).endStepJobCommitted();
        doNothing().when(cmd).releaseVolumeLock();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }

        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.CommitRemoveGlusterVolumeBricks),
                argThat(anyGlusterVolumeVDS()))).thenReturn(vdsReturnValue);
    }

    private ArgumentMatcher<VDSParametersBase> anyGlusterVolumeVDS() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterVolumeVDSParameters)) {
                    return false;
                }
                return true;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteCommand() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask)), null));
        prepareMocks(cmd);
        mockBackend(true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, times(1)).endStepJobCommitted();
        verify(cmd, times(1)).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask)), null));
        prepareMocks(cmd);
        mockBackend(false, EngineError.GlusterVolumeRemoveBricksCommitFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, never()).endStepJobCommitted();
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT_FAILED);
    }

    @Test
    public void validateSucceedsOnVolumeWithRemoveBricksTask() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask)), null));

        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithoutAsyncTask,
                        getBricks(volumeWithoutAsyncTask)), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutRemoveBricksTask() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithoutRemoveBricksTask,
                        getBricks(volumeWithoutRemoveBricksTask)), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailesOnVolumeWithRemoveBricksTaskNotFinished() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTaskNotFinished,
                        getBricks(volumeWithRemoveBricksTaskNotFinished)), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTaskNull,
                        getBricks(volumeWithRemoveBricksTaskNull)), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithEmptyBricksList() {
        cmd =
                spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithoutRemoveBricksTask,
                        new ArrayList<>()), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsWithInvalidBricks() {
        List<GlusterBrickEntity> paramBricks1 = getInvalidNoOfBricks(volumeWithRemoveBricksTask);
        cmd = spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask, paramBricks1), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());

        List<GlusterBrickEntity> paramBricks2 = getInvalidBricks(volumeWithRemoveBricksTask);
        cmd = spy(new CommitRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask, paramBricks2), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }
}
