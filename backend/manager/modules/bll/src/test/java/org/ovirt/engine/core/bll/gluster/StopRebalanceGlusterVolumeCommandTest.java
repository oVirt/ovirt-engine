package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
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
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class StopRebalanceGlusterVolumeCommandTest extends BaseCommandTest {
    @Mock
    GlusterVolumeDao volumeDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;

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
    private StopRebalanceGlusterVolumeCommand cmd;

    private void prepareMocks(StopRebalanceGlusterVolumeCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getVolumeWithRebalanceTask(volumeWithRebalanceTask)).when(volumeDao).getById(volumeWithRebalanceTask);
        doReturn(getVolumeWithRebalanceTaskCompleted(volumeWithRebalanceTaskCompleted)).when(volumeDao)
                .getById(volumeWithRebalanceTaskCompleted);
        doReturn(getVolume(volumeWithoutAsyncTask)).when(volumeDao).getById(volumeWithoutAsyncTask);
        doReturn(getvolumeWithoutRebalanceTask(volumeWithoutRebalanceTask)).when(volumeDao)
                .getById(volumeWithoutRebalanceTask);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(cluster).when(command).getCluster();
        doReturn(vdsBrokerFrontend).when(command).getVdsBroker();
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
        doReturn(backend).when(cmd).getBackend();
        doReturn("TestVDS").when(cmd).getClusterName();
        doReturn("TestVolume").when(cmd).getGlusterVolumeName();
        doNothing().when(cmd).endStepJob(argThat(jobExecutionStatus(rebalanceStopStatus)),
                argThat(anyMap()),
                booleanThat(booleanMatcher(isRebalancegTaskCompleted)));

        doNothing().when(cmd).releaseVolumeLock();
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        GlusterVolumeTaskStatusEntity rebalanceStatusEntity = new GlusterVolumeTaskStatusEntity();
        rebalanceStatusEntity.getStatusSummary().setStatus(rebalanceStopStatus);
        vdsReturnValue.setReturnValue(rebalanceStatusEntity);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.StopRebalanceGlusterVolume), argThat(anyHookVDS()))).thenReturn(vdsReturnValue);
    }

    private Matcher<Boolean> booleanMatcher(final boolean value) {
        return new ArgumentMatcher<Boolean>() {

            @Override
            public boolean matches(Object argument) {
                return argument.equals(value);
            }
        };
    }

    private Matcher<Map<String, String>> anyMap() {
        return new ArgumentMatcher<Map<String, String>>() {

            @Override
            public boolean matches(Object argument) {
                return argument instanceof Map;
            }
        };
    }

    private ArgumentMatcher<JobExecutionStatus> jobExecutionStatus(final JobExecutionStatus status) {
        return new ArgumentMatcher<JobExecutionStatus>() {

            @Override
            public boolean matches(Object argument) {
                return argument.equals(status);
            }
        };
    }

    private ArgumentMatcher<VDSParametersBase> anyHookVDS() {
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

    private StopRebalanceGlusterVolumeCommand createTestCommand(Guid volumeId) {
        return new StopRebalanceGlusterVolumeCommand(new GlusterVolumeRebalanceParameters(volumeId,
                false,
                false), null);
    }

    @Test
    public void executeCommand() {
        cmd = spy(createTestCommand(volumeWithRebalanceTask));
        prepareMocks(cmd);
        mockBackend(true, JobExecutionStatus.ABORTED, false, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd).endStepJob(argThat(jobExecutionStatus(JobExecutionStatus.ABORTED)),
                 argThat(anyMap()),
                booleanThat(booleanMatcher(false)));
        verify(cmd).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP);
    }

    @Test
    public void executeCommandWithRebalanceCompleteInNode() {
        cmd = spy(createTestCommand(volumeWithRebalanceTask));
        prepareMocks(cmd);
        mockBackend(true, JobExecutionStatus.FINISHED, true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd).endStepJob(argThat(jobExecutionStatus(JobExecutionStatus.FINISHED)),
                argThat(anyMap()),
                booleanThat(booleanMatcher(true)));
        verify(cmd).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(createTestCommand(volumeWithRebalanceTask));
        prepareMocks(cmd);
        mockBackend(false, JobExecutionStatus.FAILED, false, EngineError.GlusterVolumeRebalanceStopFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd, never()).endStepJob(any(JobExecutionStatus.class),
                anyMapOf(String.class, String.class),
                anyBoolean());
        verify(cmd, never()).releaseVolumeLock();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED);
    }

    @Test
    public void validateSucceedsOnVolumeWithRebalanceTask() {
        cmd = spy(createTestCommand(volumeWithRebalanceTask));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutAsyncTask() {
        cmd = spy(createTestCommand(volumeWithoutAsyncTask));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnVolumeWithoutRebalanceTask() {
        cmd = spy(createTestCommand(volumeWithoutRebalanceTask));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailesOnVolumeWithRebalanceTaskCompleted() {
        cmd = spy(createTestCommand(volumeWithRebalanceTaskCompleted));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(createTestCommand(null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

}
