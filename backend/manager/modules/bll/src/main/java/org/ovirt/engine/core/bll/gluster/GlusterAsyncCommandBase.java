package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

public abstract class GlusterAsyncCommandBase<T extends GlusterVolumeParameters> extends GlusterVolumeCommandBase<T> {

    private Step asyncTaskStep;

    @Inject
    private JobRepository jobRepository;
    @Inject
    protected GlusterTaskUtils glusterTaskUtils;
    @Inject
    private StepDao stepDao;
    @Inject
    protected GlusterBrickValidator brickValidator;

    public GlusterAsyncCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected boolean validate() {
        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!super.validate()) {
            return false;
        }

        if (!glusterVolume.isOnline()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    protected Map<String, String> getStepMessageMap(JobExecutionStatus status, String jobInfo) {
        Map<String, String> values = new HashMap<>();
        values.put(GlusterConstants.CLUSTER, getClusterName());
        values.put(GlusterConstants.VOLUME, getGlusterVolumeName());
        values.put(GlusterConstants.JOB_STATUS, status.toString());
        values.put(GlusterConstants.JOB_INFO, jobInfo == null ? " " : jobInfo);
        return values;
    }

    /**
     *
     * @return the StepEnum associated with this command. This is used to start a sub step for the executing step.
     */
    protected abstract StepEnum getStepType();

    protected void startSubStep() {
        asyncTaskStep =
                executionHandler.addSubStep(this.getExecutionContext(),
                        getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                        getStepType(),
                        ExecutionMessageDirector.resolveStepMessage(getStepType(),
                                getStepMessageMap(JobExecutionStatus.STARTED, null)));
    }

    protected void endStepJobAborted(String jobInfo) {
        endStepJob(JobExecutionStatus.ABORTED, getStepMessageMap(JobExecutionStatus.ABORTED, jobInfo), false);
    }

    protected void endStepJob(JobExecutionStatus status, Map<String, String> stepMessageMap, boolean exitStatus) {
        GlusterAsyncTask asyncTask = getGlusterVolume().getAsyncTask();
        // Gluster Task will be associated with only one step ( REBALANCING_VOLUME or REMOVING_BRICK)
        Step step = stepDao.getStepsByExternalId(asyncTask.getTaskId()).get(0);
        step.setStatus(status);
        step.setEndTime(new Date());
        step.setDescription(ExecutionMessageDirector.resolveStepMessage(getStepType(), stepMessageMap));
        jobRepository.updateStep(step);

        ExecutionContext finalContext = executionHandler.createFinalizingContext(step.getId());
        executionHandler.endTaskStepAndJob(finalContext, exitStatus);
    }

    protected GlusterAsyncTask handleTaskReturn(GlusterAsyncTask asyncTask) {
        Guid externalTaskId = asyncTask.getTaskId();
        asyncTaskStep.setStatus(JobExecutionStatus.STARTED);
        executionHandler.updateStepExternalId(asyncTaskStep,
                externalTaskId,
                ExternalSystemType.GLUSTER);
        getExecutionContext().getJob().setStatus(JobExecutionStatus.STARTED);
        asyncTask.setStepId(asyncTaskStep.getId());
        return asyncTask;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getGlusterVolumeId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER,
                        EngineMessage.ACTION_TYPE_FAILED_VOLUME_OPERATION_IN_PROGRESS));
    }

    protected void releaseVolumeLock() {
        EngineLock lock = new EngineLock(getExclusiveLocks(), getSharedLocks());
        setLock(lock);
        freeLock();
    }
}
