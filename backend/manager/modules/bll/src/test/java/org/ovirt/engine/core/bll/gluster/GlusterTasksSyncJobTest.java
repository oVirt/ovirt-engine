package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class, ExecutorServiceExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterTasksSyncJobTest {

    private static final Guid[] CLUSTER_GUIDS = {new Guid("CC111111-1111-1111-1111-111111111111"),
        new Guid("CC222222-2222-2222-2222-222222222222")};

    private static final Guid[] TASK_GUIDS = {new Guid("EE111111-1111-1111-1111-111111111111"),
        new Guid("EE222222-2222-2222-2222-222222222222"),
        new Guid("EE333333-3333-3333-3333-333333333333"),
        new Guid("EE444444-4444-4444-4444-444444444444")};

    private static final Guid[] VOL_GUIDS = {new Guid("AA111111-1111-1111-1111-111111111111"),
        new Guid("AA222222-2222-2222-2222-222222222222"),
        new Guid("AA333333-3333-3333-3333-333333333333")};

    @Mock
    private StepDao stepDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterTasksService provider;

    @Mock
    private GlusterTaskUtils taskUtils;

    @InjectMocks
    @Spy
    private GlusterTasksSyncJob tasksSyncJob;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.GlusterTaskMinWaitForCleanupInMins, 10));
    }

    @BeforeEach
    public void init() {
        doReturn(getClusters()).when(clusterDao).getAll();
    }

    @Test
    public void updateTasksInCluster() {
        doReturn(getTasks()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();

        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(2)).updateSteps(any(), any(), any());
    }

    @Test
    public void cleanOrphanTasks() {
        doReturn(getTasks()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        doReturn(Arrays.asList(TASK_GUIDS[2], TASK_GUIDS[3])).when(provider).getMonitoredTaskIDsInDB();
        prepareMocks();

        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(1)).endStepJob(any());
        verify(taskUtils, times(2)).updateSteps(any(), any(), any());
    }

    @Test
    public void cleanOrphanTasksWhenNoVolume() {
        doReturn(Collections.singletonList(TASK_GUIDS[2])).when(provider).getMonitoredTaskIDsInDB();
        doReturn(getSteps(TASK_GUIDS[2])).when(stepDao).getStepsByExternalId(TASK_GUIDS[2]);

        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(1)).endStepJob(any());
    }

    @Test
    public void testUpdateWhenNoTasks() {
        tasksSyncJob.updateGlusterAsyncTasks();
        verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[0], null);
        verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[1], null);
        verify(taskUtils, times(0)).endStepJob(any());
    }

    @Test
    public void testCreateTasksStartedFromCLI() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);

        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(0)).endStepJob(any());
    }

    @Test
    public void testCreateTasksStartedFromCLIWithErrors() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);

        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(0)).endStepJob(any());
    }


    @Test
    public void testUpdateWhenNoCompletedTasks() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(2)).updateSteps(any(), any(), any());
    }

    @Test
    public void testUpdateWhenAbortedTasks() {
        doReturn(getTasks(JobExecutionStatus.ABORTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
        verify(taskUtils, times(2)).updateSteps(any(), any(), any());
    }

    private void prepareMocks() {
        doReturn(getSteps(TASK_GUIDS[0])).when(stepDao).getStepsByExternalId(TASK_GUIDS[0]);
        doReturn(getSteps(TASK_GUIDS[1])).when(stepDao).getStepsByExternalId(TASK_GUIDS[1]);
        doReturn(getSteps(TASK_GUIDS[2])).when(stepDao).getStepsByExternalId(TASK_GUIDS[2]);
   }

    private List<Step> getSteps(Guid taskGuid) {
        List<Step> steps = new ArrayList<>();
        steps.add(createStep(taskGuid));
        return steps;
    }

    private Step createStep(Guid taskGuid) {
        Step step = new Step();
        step.setStepType(StepEnum.REBALANCING_VOLUME);
        Calendar stepTime = Calendar.getInstance();
        if (taskGuid.equals(TASK_GUIDS[2])) {
            //only create TASK_GUIDS[2] as older job
            stepTime.set(Calendar.HOUR, stepTime.get(Calendar.HOUR) -1);
        }
        step.setStartTime(stepTime.getTime());
        return step;
    }

    private Map<Guid, GlusterAsyncTask> getTasks() {
        Map<Guid, GlusterAsyncTask> tasks = new HashMap<>();
        tasks.put(TASK_GUIDS[0], createTask(TASK_GUIDS[0], JobExecutionStatus.FINISHED));
        tasks.put(TASK_GUIDS[1], createTask(TASK_GUIDS[1], JobExecutionStatus.STARTED));
        return tasks;
    }

    private Map<Guid, GlusterAsyncTask> getTasks(JobExecutionStatus status) {
        Map<Guid, GlusterAsyncTask> tasks = new HashMap<>();
        tasks.put(TASK_GUIDS[0], createTask(TASK_GUIDS[0], status));
        tasks.put(TASK_GUIDS[1], createTask(TASK_GUIDS[1], status));
        return tasks;
    }

    private GlusterAsyncTask createTask(Guid guid, JobExecutionStatus status) {
        GlusterAsyncTask task = new GlusterAsyncTask();
        task.setTaskId(guid);
        task.setTaskParameters(new GlusterTaskParameters());
        task.getTaskParameters().setVolumeName("VOL");
        task.setMessage("message");
        task.setStatus(status);
        task.setType(GlusterTaskType.REBALANCE);
        return task;
    }

    private List<Cluster> getClusters() {
        List<Cluster> list = new ArrayList<>();
        list.add(createCluster(0, Version.v4_2));
        list.add(createCluster(1, Version.v4_3));
        return list;
    }

    private Cluster createCluster(int index, Version v) {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_GUIDS[index]);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(v);
        return cluster;
    }
}
