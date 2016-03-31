package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
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

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private StepDao stepDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterTasksService provider;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private BackendInternal backend;

    private GlusterTaskUtils taskUtils;

    @Mock
    private GlusterAuditLogUtil logUtil;

    private GlusterTasksSyncJob tasksSyncJob;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10),
            mockConfig(ConfigValues.GlusterTaskMinWaitForCleanupInMins, 10));

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        tasksSyncJob = Mockito.spy(GlusterTasksSyncJob.getInstance());
        taskUtils = Mockito.spy(GlusterTaskUtils.getInstance());
        doNothing().when(logUtil).logClusterMessage(any(Guid.class),
                                any(AuditLogType.class));
        doReturn(clusterDao).when(tasksSyncJob).getClusterDao();
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(provider).when(tasksSyncJob).getProvider();
        doReturn(stepDao).when(tasksSyncJob).getStepDao();
        doReturn(volumeDao).when(tasksSyncJob).getVolumeDao();
        doReturn(volumeDao).when(taskUtils).getVolumeDao();
        doReturn(jobRepository).when(tasksSyncJob).getJobRepository();
        doReturn(jobRepository).when(taskUtils).getJobRepository();
        doReturn(backend).when(tasksSyncJob).getBackend();
        doReturn(taskUtils).when(tasksSyncJob).getGlusterTaskUtils();
        doReturn(logUtil).when(tasksSyncJob).getGlusterLogUtil();
        doNothing().when(taskUtils).releaseLock(any(Guid.class));
        doNothing().when(taskUtils).endStepJob(any(Step.class));
        doReturn(null).when(provider).getMonitoredTaskIDsInDB();
        doNothing().when(taskUtils).logEventMessage(any(GlusterAsyncTask.class), any(JobExecutionStatus.class), any(Cluster.class));
        doNothing().when(logUtil).logAuditMessage(any(Guid.class),
                any(GlusterVolumeEntity.class),
                any(VDS.class),
                any(AuditLogType.class),
                any(HashMap.class));
    }

    @Test
    public void updateTasksInCluster() {
        doReturn(getTasks()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(1)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(1)).endStepJob(any(Step.class));
    }

    @Test
    public void cleanOrphanTasks() {
        doReturn(getTasks()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        doReturn(Arrays.asList(TASK_GUIDS[2], TASK_GUIDS[3])).when(provider).getMonitoredTaskIDsInDB();
        prepareMocks();

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(1)).updateStep(any(Step.class));
        //endStepJob will not be called for TASK_GUIDS[3], so times is = 2
        Mockito.verify(taskUtils, times(2)).endStepJob(any(Step.class));
    }

    @Test
    public void cleanOrphanTasksWhenNoVolume() {
        doReturn(new HashMap<>()).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        doReturn(Arrays.asList(TASK_GUIDS[2])).when(provider).getMonitoredTaskIDsInDB();
        doReturn(null).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[2]);
        doReturn(getSteps(TASK_GUIDS[2])).when(stepDao).getStepsByExternalId(TASK_GUIDS[2]);

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(taskUtils, times(1)).endStepJob(any(Step.class));
    }

    @Test
    public void testUpdateWhenNoTasks() {
        doReturn(null).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[0], null);
        Mockito.verify(volumeDao, times(0)).updateVolumeTask(VOL_GUIDS[1], null);
        Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(0)).endStepJob(any(Step.class));
    }

    @Test
    public void testCreateTasksStartedFromCLI() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        doReturn(mockVdcReturn(true, Guid.newGuid())).
                        when(backend).runInternalAction(any(VdcActionType.class), any(VdcActionParametersBase.class));
        prepareMocksForTasksFromCLI();

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(0)).endStepJob(any(Step.class));
    }

    @Test
    public void testCreateTasksStartedFromCLIWithErrors() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        doReturn(mockVdcReturn(false, null)).
                        when(backend).runInternalAction(any(VdcActionType.class), any(VdcActionParametersBase.class));
        prepareMocksForTasksFromCLI();

        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(0)).endStepJob(any(Step.class));
    }


    @Test
    public void testUpdateWhenNoCompletedTasks() {
        doReturn(getTasks(JobExecutionStatus.STARTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(2)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(0)).endStepJob(any(Step.class));
    }

    @Test
    public void testUpdateWhenAbortedTasks() {
        doReturn(getTasks(JobExecutionStatus.ABORTED)).when(provider).getTaskListForCluster(CLUSTER_GUIDS[1]);
        prepareMocks();
        tasksSyncJob.updateGlusterAsyncTasks();
        Mockito.verify(jobRepository, times(0)).updateStep(any(Step.class));
        Mockito.verify(taskUtils, times(2)).endStepJob(any(Step.class));
    }

    private void prepareMocks() {
        doReturn(getVolume(0)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[0]);
        doReturn(getVolume(1)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[1]);
        doReturn(getVolume(1)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[2]);
        doReturn(getSteps(TASK_GUIDS[0])).when(stepDao).getStepsByExternalId(TASK_GUIDS[0]);
        doReturn(getSteps(TASK_GUIDS[1])).when(stepDao).getStepsByExternalId(TASK_GUIDS[1]);
        doReturn(getSteps(TASK_GUIDS[2])).when(stepDao).getStepsByExternalId(TASK_GUIDS[2]);
   }

    private void prepareMocksForTasksFromCLI() {
        doReturn(getVolume(0)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[0]);
        doReturn(getVolume(1)).when(volumeDao).getVolumeByGlusterTask(TASK_GUIDS[1]);
        doReturn(new ArrayList<Step>()).when(stepDao).getStepsByExternalId(TASK_GUIDS[0]);
        doReturn(new ArrayList<Step>()).when(stepDao).getStepsByExternalId(TASK_GUIDS[1]);
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

    private GlusterVolumeEntity getVolume(int i) {
        GlusterVolumeEntity vol = new GlusterVolumeEntity();
        vol.setStatus(GlusterStatus.UP);
        vol.setId(VOL_GUIDS[i]);
        vol.setClusterId(CLUSTER_GUIDS[1]);
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

    private VdcReturnValueBase mockVdcReturn(boolean succeeded, Guid returnId) {
        VdcReturnValueBase retValue = new VdcReturnValueBase();
        retValue.setSucceeded(succeeded);
        retValue.setActionReturnValue(returnId);
        return retValue;
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
        list.add(createCluster(0, Version.v3_6));
        list.add(createCluster(1, Version.v4_0));
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
