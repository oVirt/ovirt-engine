package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTasksService;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.action.AddInternalJobParameters;
import org.ovirt.engine.core.common.action.AddStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
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

    public JobRepository getJobRepository() {
        return JobRepositoryFactory.getJobRepository();
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
        if (!supportsGlusterAsyncTasksFeature(cluster))
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
                            createJobToMonitor(cluster,task);
                        } catch (VdcBLLException e) {
                            log.error("Error creating job for task from CLI", e);
                        }
                        return null;
                    }
                });
            }
            //update status in step table
            for (Step step: steps) {
                if (step.getEndTime() != null) {
                    //we have already processed the task
                    continue;
                }
                step.setDescription(getTaskMessage(cluster,step.getStepType(),task));
                if (hasTaskCompleted(task)) {
                    step.markStepEnded(task.getStatus());
                    endStepJob(step);
                    releaseVolumeLock(task.getTaskId());
                } else {
                    getJobRepository().updateStep(step);
                }
            }


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

    }

    private Guid addAsyncTaskStep(VDSGroup cluster, GlusterAsyncTask task, StepEnum step, Guid execStepId) {
        VdcReturnValueBase result;
        result = getBackend().runInternalAction(VdcActionType.AddInternalStep,
                new AddStepParameters(execStepId, getTaskMessage(cluster, step, task), step));

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
                new AddInternalJobParameters(ExecutionMessageDirector.resolveJobMessage(actionType, getMessageMap(cluster, task)),
                        actionType, true) );
        if (!result.getSucceeded()) {
            //log and return
            throw new VdcBLLException(result.getFault().getError());
        }
        Guid jobId = (Guid)result.getActionReturnValue();
        return jobId;
    }

    private void releaseVolumeLock(Guid taskId) {
        //get volume associated with task
        GlusterVolumeEntity vol= getVolumeDao().getVolumeByGlusterTask(taskId);

        if (vol != null) {
            //release lock on volume
            releaseLock(vol.getId());

        } else {
            log.debugFormat("Did not find a volume associated with task {0}", taskId);
        }
    }

    protected void endStepJob(Step step) {
        getJobRepository().updateStep(step);
        ExecutionContext finalContext = ExecutionHandler.createFinalizingContext(step.getId());
        ExecutionHandler.endTaskJob(finalContext, isTaskSuccess(step));
    }

    private static boolean isTaskSuccess(Step step) {
        switch (step.getStatus()) {
        case ABORTED:
        case FAILED:
            return false;
        case FINISHED:
            return true;
        default:
            return false;
        }
    }

    private static boolean hasTaskCompleted(GlusterAsyncTask task) {
        if (JobExecutionStatus.ABORTED == task.getStatus() || JobExecutionStatus.FINISHED == task.getStatus()
                || JobExecutionStatus.FAILED == task.getStatus()) {
            return true;
        }
        return false;
    }

    private static String getTaskMessage(VDSGroup cluster, StepEnum stepType, GlusterAsyncTask task) {
        if (task==null) {
            return null;
        }
        Map<String, String> values = getMessageMap(cluster, task);

        return ExecutionMessageDirector.resolveStepMessage(stepType, values);
    }

    private static Map<String, String> getMessageMap(VDSGroup cluster, GlusterAsyncTask task) {
        Map<String, String> values = new HashMap<String, String>();
        values.put(GlusterConstants.CLUSTER, cluster.getName());
        values.put(GlusterConstants.VOLUME, task.getTaskParameters().getVolumeName());
        values.put("status", task.getStatus().toString());
        values.put("info", task.getMessage());
        return values;
    }

    private boolean supportsGlusterAsyncTasksFeature(VDSGroup cluster) {
        return cluster.supportsGlusterService() && GlusterFeatureSupported.glusterAsyncTasks(cluster.getcompatibility_version());
    }
}
