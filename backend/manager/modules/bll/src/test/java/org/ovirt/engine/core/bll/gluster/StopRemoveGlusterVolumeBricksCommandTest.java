package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class StopRemoveGlusterVolumeBricksCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;

    private final Guid volumeWithRemoveBricksTask = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private final Guid volumeWithRemoveBricksTaskCompleted = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeWithoutAsyncTask = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid volumeWithoutRemoveBricksTask = new Guid("000000000000-0000-0000-0000-00000004");
    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    /**
     * The command under test.
     */
    private StopRemoveGlusterVolumeBricksCommand cmd;

    private void prepareMocks(StopRemoveGlusterVolumeBricksCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getVolumeWithRemoveBricksTask(volumeWithRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithRemoveBricksTask);
        doReturn(getVolumeWithRemoveBricksTaskCompleted(volumeWithRemoveBricksTaskCompleted)).when(volumeDao)
                .getById(volumeWithRemoveBricksTaskCompleted);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getvolumeWithoutRemoveBricksTask(volumeWithoutRemoveBricksTask)).when(volumeDao)
                .getById(volumeWithoutRemoveBricksTask);
        doReturn(null).when(volumeDao).getById(null);
    }

    private Object getvolumeWithoutRemoveBricksTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRemoveBricksTask(volumeId);
        volume.getAsyncTask().setType(null);
        return volume;
    }

    private Object getVolumeWithRemoveBricksTaskCompleted(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRemoveBricksTask(volumeId);
        volume.getAsyncTask().setStatus(JobExecutionStatus.FINISHED);
        return volume;
    }

    private GlusterVolumeEntity getVolumeWithRemoveBricksTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setStatus(JobExecutionStatus.STARTED);
        asyncTask.setType(GlusterTaskType.REMOVE_BRICK);
        volume.setAsyncTask(asyncTask);
        return volume;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
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
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
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

    private void mockBackend(boolean succeeded, VdcBllErrors errorCode) {
        when(cmd.getBackend()).thenReturn(backend);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
        doNothing().when(cmd).endStepJobAborted();
        doNothing().when(cmd).releaseVolumeLock();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        } else {
            GlusterAsyncTask task = new GlusterAsyncTask();
            task.setMessage("successful");
            task.setStatus(JobExecutionStatus.FINISHED);
            task.setStepId(Guid.newGuid());
            task.setTaskId(Guid.newGuid());
            task.setType(GlusterTaskType.REMOVE_BRICK);

            vdsReturnValue.setReturnValue(task);
        }

        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.StopRemoveGlusterVolumeBricks),
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

    @Test
    public void testExecuteCommand() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask, 2))));
        prepareMocks(cmd);
        mockBackend(true, null);
        assertTrue(cmd.canDoAction());
        cmd.executeCommand();

        verify(cmd, times(1)).endStepJobAborted();
        verify(cmd, times(1)).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask, 2))));
        prepareMocks(cmd);
        mockBackend(false, VdcBllErrors.GlusterVolumeRemoveBricksStopFailed);
        assertTrue(cmd.canDoAction());
        cmd.executeCommand();

        verify(cmd, never()).endStepJobAborted();
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED);
    }

    @Test
    public void canDoActionSucceedsOnVolumeWithRemoveBricksTask() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTask,
                        getBricks(volumeWithRemoveBricksTask, 2))));

        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    // This happens in retain bricks scenario, where user can call stop remove brick after migrating the data
    @Test
    public void canDoActionSucceedsOnVolumeWithRemoveBricksTaskCompleted() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithRemoveBricksTaskCompleted,
                        getBricks(volumeWithRemoveBricksTaskCompleted, 2))));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnVolumeWithoutAsyncTask() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithoutAsyncTask,
                        getBricks(volumeWithoutAsyncTask, 2))));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnVolumeWithoutRemoveBricksTask() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(volumeWithoutRemoveBricksTask,
                        getBricks(volumeWithoutRemoveBricksTask, 2))));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd =
                spy(new StopRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(null,
                        getBricks(volumeWithRemoveBricksTaskCompleted, 2))));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }
}
