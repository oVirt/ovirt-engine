package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddInternalJobParameters;
import org.ovirt.engine.core.common.action.AddStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterTasksSyncJob extends GlusterJob  {
    private static final Logger log = LoggerFactory.getLogger(GlusterTasksSyncJob.class);

    private static GlusterTasksSyncJob instance = new GlusterTasksSyncJob();

    private final GlusterTasksService provider = new GlusterTasksService();

    public static GlusterTasksSyncJob getInstance() {
        return instance;
    }

    public void init() {
        log.info("Gluster task manager has been initialized");
    }

    public GlusterTasksService getProvider() {
        return provider;
    }

    public BackendInternal getBackend() {
        return Backend.getInstance();
    }

    @OnTimerMethodAnnotation("gluster_async_task_poll_event")
    public void updateGlusterAsyncTasks() {
        log.debug("Refreshing gluster tasks list");
        List<Cluster> clusters = getClusterDao().getAll();

        Map<Guid, Set<Guid>> tasksFromClustersMap = new HashMap<>();
        for (Cluster cluster : clusters) {
            if (!cluster.supportsGlusterService()) {
                continue;
            }
            try {
                Map<Guid, GlusterAsyncTask> runningTasks = getProvider().getTaskListForCluster(cluster.getId());
                if (runningTasks != null) {
                    updateTasksInCluster(cluster, runningTasks);
                    tasksFromClustersMap.put(cluster.getId(), runningTasks.keySet());
                }
            } catch (EngineException e) {
                log.error("Error updating tasks from CLI", e);
            }
        }

        cleanUpOrphanTasks(tasksFromClustersMap);
    }

    private void updateTasksInCluster(final Cluster cluster, final Map<Guid, GlusterAsyncTask> runningTasks) {

        for  (Entry<Guid, GlusterAsyncTask> entry :  runningTasks.entrySet()) {
            Guid taskId = entry.getKey();
            final GlusterAsyncTask task =  entry.getValue();

            List<Step> steps = getStepDao().getStepsByExternalId(taskId);

            if (steps.isEmpty()) {
                createJobForTaskFromCLI(cluster, task);
            }
            getGlusterTaskUtils().updateSteps(cluster, task, steps);
        }
    }

    private void createJobForTaskFromCLI(final Cluster cluster, final GlusterAsyncTask task) {
        ThreadPoolUtil.execute(() -> TransactionSupport.executeInNewTransaction(() -> {
            try {
                createJobToMonitor(cluster, task);
            } catch (EngineException e) {
                log.error("Error creating job for task from CLI", e);
            }
            return null;
        }));
    }

    private void createJobToMonitor(Cluster cluster, GlusterAsyncTask task) {
        if (!isTaskToBeMonitored(task)) {
            return; //there's no need to monitor jobs that are failed or completed
        }
        StepEnum step = task.getType().getStep();
        VdcActionType actionType;
        switch (step) {
        case REBALANCING_VOLUME:
            actionType = VdcActionType.StartRebalanceGlusterVolume;
            break;
        case REMOVING_BRICKS:
            actionType = VdcActionType.StartRemoveGlusterVolumeBricks;
            break;
        default:
            actionType = VdcActionType.Unknown;
        }

        String volumeName = task.getTaskParameters().getVolumeName();
        GlusterVolumeEntity vol = getVolumeDao().getByName(cluster.getId(), volumeName);

        if (vol == null) {
            log.info("Volume '{}' does not exist yet for task detected from CLI '{}', not adding to engine",
                    volumeName, task);
            return;
        }

        Guid jobId = addJob(cluster, task, actionType, vol);

        Guid execStepId = addExecutingStep(jobId);

        Guid asyncStepId = addAsyncTaskStep(cluster, task, step, execStepId);
        Step asyncStep = getStepDao().get(asyncStepId);
        ExecutionHandler.updateStepExternalId(asyncStep,
                task.getTaskId(),
                ExternalSystemType.GLUSTER);
        updateVolumeBricksAndLock(cluster, task, vol);
    }

    private boolean isTaskToBeMonitored(GlusterAsyncTask task) {
        return task.getStatus() == JobExecutionStatus.STARTED || task.getType() == GlusterTaskType.REMOVE_BRICK;
    }

    private Guid addAsyncTaskStep(Cluster cluster, GlusterAsyncTask task, StepEnum step, Guid execStepId) {
        VdcReturnValueBase result;
        result = getBackend().runInternalAction(VdcActionType.AddInternalStep,
                new AddStepParameters(execStepId, getGlusterTaskUtils().getTaskMessage(cluster, step, task), step));

        if (!result.getSucceeded()) {
            //log and return
            throw new EngineException(result.getFault().getError());
        }

        return result.getActionReturnValue();
    }

    private Guid addExecutingStep(Guid jobId) {
        VdcReturnValueBase result;
        result = getBackend().runInternalAction(VdcActionType.AddInternalStep,
                new AddStepParameters(jobId, ExecutionMessageDirector.resolveStepMessage(StepEnum.EXECUTING, null), StepEnum.EXECUTING));
        if (!result.getSucceeded()) {
            //log and return
            throw new EngineException(result.getFault().getError());
        }

        return result.getActionReturnValue();
    }

    private Guid addJob(Cluster cluster, GlusterAsyncTask task, VdcActionType actionType, final GlusterVolumeEntity vol) {

        VdcReturnValueBase result = getBackend().runInternalAction(VdcActionType.AddInternalJob,
                new AddInternalJobParameters(ExecutionMessageDirector.resolveJobMessage(actionType, getGlusterTaskUtils().getMessageMap(cluster, task)),
                        actionType, true, VdcObjectType.GlusterVolume, vol.getId()) );
        if (!result.getSucceeded()) {
            //log and return
            throw new EngineException(result.getFault().getError());
        }
        return result.getActionReturnValue();
    }

    private void updateVolumeBricksAndLock(Cluster cluster, GlusterAsyncTask task, final GlusterVolumeEntity vol) {

        try {
            //acquire lock on volume
            acquireLock(vol.getId());

            //update volume with task id
            getVolumeDao().updateVolumeTask(vol.getId(), task.getTaskId());

            if (GlusterTaskType.REMOVE_BRICK == task.getType()) {
                //update bricks associated with task id
                String[] bricks = task.getTaskParameters().getBricks();

                if (bricks != null) {
                    List<GlusterBrickEntity> brickEntities = new ArrayList<>();
                    for (String brick: bricks) {
                        String[] brickParts = brick.split(":", -1);
                        String hostnameOrIp = brickParts[0];
                        String brickDir = brickParts[1];
                        GlusterBrickEntity brickEntity = new GlusterBrickEntity();
                        VdsStatic server = GlusterDBUtils.getInstance().getServer(cluster.getId(), hostnameOrIp);
                        if (server == null) {
                            log.warn("Could not find server '{}' in cluster '{}'", hostnameOrIp, cluster.getId());
                        } else {
                            brickEntity.setServerId(server.getId());
                            brickEntity.setBrickDirectory(brickDir);
                            brickEntity.setAsyncTask(new GlusterAsyncTask());
                            brickEntity.getAsyncTask().setTaskId(task.getTaskId());
                            brickEntities.add(brickEntity);
                        }
                    }
                    getBrickDao().updateAllBrickTasksByHostIdBrickDirInBatch(brickEntities);
                }
            }
            logTaskStartedFromCLI(cluster, task, vol);
        } catch (Exception e) {
            log.error("Exception", e);
            // Release the lock only if there is any exception,
            // otherwise the lock will be released once the task is completed
            releaseLock(vol.getId());
            throw new EngineException(EngineError.GeneralException, e.getMessage());
        }
    }

    private void logTaskStartedFromCLI(Cluster cluster, GlusterAsyncTask task, GlusterVolumeEntity vol) {
        Map<String, String> values = new HashMap<>();

        AuditLogType logType;
        switch (task.getType()) {
        case REBALANCE:
            logType = AuditLogType.GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI;
            break;
        case REMOVE_BRICK:
            logType = AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI;
            values.put(GlusterConstants.BRICK, StringUtils.join(task.getTaskParameters().getBricks(), ','));
            break;
         default:
            logType = AuditLogType.UNASSIGNED;
            break;
        }
        getGlusterLogUtil().logAuditMessage(cluster.getId(), vol, null, logType, values);
    }

    private void logTaskStoppedFromCLI(Step step, GlusterVolumeEntity vol) {
        AuditLogType logType;
        switch (step.getStepType()) {
        case REBALANCING_VOLUME:
            logType = AuditLogType.GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI;
            break;
        case REMOVING_BRICKS:
            logType = AuditLogType.REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI;
             break;
         default:
            logType = AuditLogType.UNASSIGNED;
            break;
        }
        getGlusterLogUtil().logAuditMessage(vol.getClusterId(), vol, null, logType, null);
    }

    protected GlusterAuditLogUtil getGlusterLogUtil() {
        return GlusterAuditLogUtil.getInstance();
    }

    public GlusterTaskUtils getGlusterTaskUtils() {
        return GlusterTaskUtils.getInstance();
    }

    public JobRepository getJobRepository() {
        return JobRepositoryFactory.getJobRepository();
    }

    /**
     * This method cleans the tasks in DB which the gluster CLI is no longer
     * aware of.
     * @param runningTasksInClusterMap - map of cluster id - task list in cluster
     */
    private void cleanUpOrphanTasks(Map<Guid, Set<Guid>> runningTasksInClusterMap) {
        // if map is empty, no tasks from clusters fetched. so return
        if (runningTasksInClusterMap.isEmpty()) {
            log.debug("Clean up of tasks has been skipped");
            return;
        }
        //Populate the list of tasks that need to be monitored from database
        List<Guid> taskListInDB = getProvider().getMonitoredTaskIDsInDB();
        if (taskListInDB == null || taskListInDB.isEmpty()) {
            return;
        }

        Set<Guid> allRunningTasksInCluster = new HashSet<>();
        for (Set<Guid> taskSet: runningTasksInClusterMap.values()) {
            if (taskSet != null) {
                allRunningTasksInCluster.addAll(taskSet);
            }
        }

        //if task is in DB but not in running task list
        final Set<Guid> tasksNotRunning = new HashSet<>(taskListInDB);
        tasksNotRunning.removeAll(allRunningTasksInCluster);
        log.debug("Tasks to be cleaned up in db '{}'", tasksNotRunning);

        for (Guid taskId: tasksNotRunning) {
            GlusterVolumeEntity vol= getVolumeDao().getVolumeByGlusterTask(taskId);
            if (vol != null
                    && (vol.getStatus() != GlusterStatus.UP || !runningTasksInClusterMap.keySet()
                            .contains(vol.getClusterId()))) {
                // the volume is not UP. Hence gluster may not have been able to return tasks for the volume
                // also handling the case where gluster was not able to return any tasks from this cluster - the keyset will not
                // contain the cluster id in such case
                continue;
            }

            //Volume is up, but gluster does not know of task
            //will mark job ended with status unknown.
            List<Step> steps = getStepDao().getStepsByExternalId(taskId);
            Map<String, String> values = new HashMap<>();
            values.put(GlusterConstants.CLUSTER, vol == null ? "" :vol.getClusterName());
            values.put(GlusterConstants.VOLUME, vol == null ? "" : vol.getName());
            values.put(GlusterConstants.JOB_STATUS, JobExecutionStatus.UNKNOWN.toString());
            values.put(GlusterConstants.JOB_INFO, " ");

            for (Step step: steps) {
                if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - step.getStartTime().getTime()) < getMininumWaitInMins()) {
                    //This task has been recently created. We will give it 10 mins before clearing it.
                    continue;
                }
                step.markStepEnded(JobExecutionStatus.UNKNOWN);
                step.setStatus(JobExecutionStatus.UNKNOWN);
                step.setDescription(ExecutionMessageDirector.resolveStepMessage(step.getStepType(), values));
                getGlusterTaskUtils().endStepJob(step);
                if (vol != null) {
                    logTaskStoppedFromCLI(step, vol);
                }
            }
            getGlusterTaskUtils().releaseVolumeLock(taskId);
        }

    }

    private static Integer getMininumWaitInMins() {
        return Config.<Integer> getValue(ConfigValues.GlusterTaskMinWaitForCleanupInMins);
    }
}
