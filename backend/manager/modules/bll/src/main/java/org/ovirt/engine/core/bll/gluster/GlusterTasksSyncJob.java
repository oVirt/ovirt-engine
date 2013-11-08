package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.action.AddInternalJobParameters;
import org.ovirt.engine.core.common.action.AddStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class GlusterTasksSyncJob extends GlusterJob  {
    private static final Log log = LogFactory.getLog(GlusterTasksSyncJob.class);

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
        List<VDSGroup> clusters = getClusterDao().getAll();

        for (VDSGroup cluster : clusters) {

            updateTasksInCluster(cluster);
        }

    }

    private Map<Guid, GlusterAsyncTask> updateTasksInCluster(final VDSGroup cluster) {
        if (!getGlusterTaskUtils().supportsGlusterAsyncTasksFeature(cluster))
        {
            return null;
        }

        Map<Guid, GlusterAsyncTask> runningTasks = getProvider().getTaskListForCluster(cluster.getId());

        if (runningTasks == null) {
            return null;
        }

        for  (Entry<Guid, GlusterAsyncTask> entry :  runningTasks.entrySet()) {
            Guid taskId = entry.getKey();
            final GlusterAsyncTask task =  entry.getValue();

            List<Step> steps = getStepDao().getStepsByExternalId(taskId);
            if (steps.isEmpty()) {
                // the task may have been started from the CLI
                // need to add job to monitor
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        try {
                            createJobToMonitor(cluster, task);
                        } catch (VdcBLLException e) {
                            log.error("Error creating job for task from CLI", e);
                        }
                        return null;
                    }
                });
            }
            getGlusterTaskUtils().updateSteps(cluster, task, steps);
        }

        return runningTasks;
    }


    private void createJobToMonitor(VDSGroup cluster, GlusterAsyncTask task) {
        if (task.getStatus() != JobExecutionStatus.STARTED) {
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

        Guid jobId = addJob(cluster, task, actionType);

        Guid execStepId = addExecutingStep(jobId);

        Guid asyncStepId = addAsyncTaskStep(cluster, task, step, execStepId);
        Step asyncStep = getStepDao().get(asyncStepId);
        ExecutionHandler.updateStepExternalId(asyncStep,
                task.getTaskId(),
                ExternalSystemType.GLUSTER);
        updateVolumeBricksAndLock(cluster, task);

    }

    private Guid addAsyncTaskStep(VDSGroup cluster, GlusterAsyncTask task, StepEnum step, Guid execStepId) {
        VdcReturnValueBase result;
        result = getBackend().runInternalAction(VdcActionType.AddInternalStep,
                new AddStepParameters(execStepId, getGlusterTaskUtils().getTaskMessage(cluster, step, task), step));

        if (!result.getSucceeded()) {
            //log and return
            throw new VdcBLLException(result.getFault().getError());
        }

        Guid asyncStepId = (Guid)result.getActionReturnValue();
        return asyncStepId;
    }

    private Guid addExecutingStep(Guid jobId) {
        VdcReturnValueBase result;
        result = getBackend().runInternalAction(VdcActionType.AddInternalStep,
                new AddStepParameters(jobId, ExecutionMessageDirector.resolveStepMessage(StepEnum.EXECUTING, null), StepEnum.EXECUTING));
        if (!result.getSucceeded()) {
            //log and return
            throw new VdcBLLException(result.getFault().getError());
        }

        Guid execStepId = (Guid)result.getActionReturnValue();
        return execStepId;
    }

    private Guid addJob(VDSGroup cluster, GlusterAsyncTask task, VdcActionType actionType) {
        VdcReturnValueBase result = getBackend().runInternalAction(VdcActionType.AddInternalJob,
                new AddInternalJobParameters(ExecutionMessageDirector.resolveJobMessage(actionType, getGlusterTaskUtils().getMessageMap(cluster, task)),
                        actionType, true) );
        if (!result.getSucceeded()) {
            //log and return
            throw new VdcBLLException(result.getFault().getError());
        }
        Guid jobId = (Guid)result.getActionReturnValue();
        return jobId;
    }

    private void updateVolumeBricksAndLock(VDSGroup cluster, GlusterAsyncTask task) {
        //get volume associated with task
        String volumeName = task.getTaskParameters().getVolumeName();
        GlusterVolumeEntity vol = getVolumeDao().getByName(cluster.getId(), volumeName);

        if (vol != null) {
            //update volume with task id
            getVolumeDao().updateVolumeTask(vol.getId(), task.getTaskId());
            //acquire lock on volume
            acquireLock(vol.getId());

            if (GlusterTaskType.REMOVE_BRICK == task.getType()) {
                //update bricks associated with task id
                String[] bricks = task.getTaskParameters().getBricks();
                if (bricks == null)
                {
                    return;
                }
                List<GlusterBrickEntity> brickEntities = new ArrayList<>();
                for (String brick: bricks) {
                    String[] brickParts = brick.split(":", -1);
                    String hostnameOrIp = brickParts[0];
                    String brickDir = brickParts[1];
                    GlusterBrickEntity brickEntity = new GlusterBrickEntity();
                    VdsStatic server = GlusterDBUtils.getInstance().getServer(cluster.getId(), hostnameOrIp);
                    if (server == null) {
                        log.warnFormat("Could not find server {0} in cluster {1}", hostnameOrIp, cluster.getId());
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

        } else {
            log.debugFormat("Did not find a volume associated with volumeName {0} and task {1} ",
                                volumeName, task.getTaskId());
        }
    }

    public GlusterTaskUtils getGlusterTaskUtils() {
        return GlusterTaskUtils.getInstance();
    }

    public JobRepository getJobRepository() {
        return JobRepositoryFactory.getJobRepository();
    }
}
