package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class GlusterTasksSyncJobTest {
    private static final Guid[] CLUSTER_GUIDS = {new Guid("CC111111-1111-1111-1111-111111111111"),
        new Guid("CC222222-2222-2222-2222-222222222222")};

    private static final Guid[] TASK_GUIDS = {new Guid("EE111111-1111-1111-1111-111111111111"),
        new Guid("EE222222-2222-2222-2222-222222222222")};

    private static final Guid[] VOL_GUIDS = {new Guid("AA111111-1111-1111-1111-111111111111"),
        new Guid("AA222222-2222-2222-2222-222222222222")};

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private StepDao stepDao;

    @Mock
    private VdsGroupDAO clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterTasksService provider;

    @Mock
    private JobRepository jobRepository;

    private GlusterTasksSyncJob tasksSyncJob;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterAysncTasksSupport, Version.v3_2.toString(), false),
            mockConfig(ConfigValues.GlusterAysncTasksSupport, Version.v3_3.toString(), true));

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        tasksSyncJob = Mockito.spy(GlusterTasksSyncJob.getInstance());
        doReturn(clusterDao).when(tasksSyncJob).getClusterDao();
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(provider).when(tasksSyncJob).getProvider();
        doReturn(stepDao).when(tasksSyncJob).getStepDao();
        doReturn(volumeDao).when(tasksSyncJob).getVolumeDao();
        doReturn(jobRepository).when(tasksSyncJob).getJobRepository();
        doNothing().when(tasksSyncJob).releaseLock(any(Guid.class));
        doNothing().when(tasksSyncJob).endStepJob(any(Step.class));
    }

    @Test
    public void updateTasksInCluster() {
        doReturn(getTasks()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(1)).updateStep(any(Step.class));
        Mockito.verify(tasksSyncJob, times(1)).endStepJob(any(Step.class));
    }

    @Test
    public void testUpdateWhenNoTasks() {
        doReturn(null).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[0],null);
        Mockito.verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[1],null);
        Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(tasksSyncJob, times(0)).endStepJob(any(Step.class));
    }

    @Test
    public void testUpdateWhenNoCompletedTasks() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(2)).updateStep(any(Step.class));
        Mockito.verify(tasksSyncJob, times(0)).endStepJob(any(Step.class));
    }

    @Test
    public void testUpdateWhenAbortedTasks() {
        doReturn(getTasks(JobExecutionStatus.ABORTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
          Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(tasksSyncJob, times(2)).endStepJob(any(Step.class));
    }

    private void prepareMocks() {
        doReturn(getVolume(0)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[0]);
        doReturn(getVolume(1)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[1]);
        doReturn(getSteps()).when(stepDao).getStepsByExternalId(TASK_GUIDS[0]);
        doReturn(getSteps()).when(stepDao).getStepsByExternalId(TASK_GUIDS[1]);
    }

    private List<Step> getSteps() {
        List<Step> steps = new ArrayList<>();
        steps.add(createStep());
        return steps;
    }


    private Step createStep() {
        Step step = new Step();
        step.setStepType(StepEnum.REBALANCING_VOLUME);
        return step;
    }

    private GlusterVolumeEntity getVolume(int i) {
        GlusterVolumeEntity vol = new GlusterVolumeEntity();
        vol.setId(VOL_GUIDS[i]);
        return vol;
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
        return task;
    }

    private List<VDSGroup> getClusters() {
        List<VDSGroup> list = new ArrayList<>();
        list.add(createCluster(0, Version.v3_2));
        list.add(createCluster(1, Version.v3_3));
        return list;
    }

    private VDSGroup createCluster(int index, Version v) {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(CLUSTER_GUIDS[index]);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setcompatibility_version(v);
        return cluster;
    }

}
