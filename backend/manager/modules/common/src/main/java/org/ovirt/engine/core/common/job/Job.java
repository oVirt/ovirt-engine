package org.ovirt.engine.core.common.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents the Job entity which encapsulates a client action in the system. The Job contains a collection
 * of steps which describe portions of the entire Job. The Job entity is capable to produce a descriptive tree of
 * steps, reflecting the action parts.
 */
public class Job implements Queryable, BusinessEntity<Guid> {

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 9003809286645635497L;

    /**
     * The Job ID uniquely identifies a disk in the system.
     */
    private Guid id;

    /**
     * The action type which the Job describes
     */
    private ActionType actionType;

    /**
     * The description of the job
     */
    private String description;

    /**
     * The status of the job
     */
    private JobExecutionStatus status;

    /**
     * The user id which invoked the job
     */
    private Guid ownerId;

    /**
     * The engine session seq id of the user which invoked the job
     */
    private long engineSessionSeqId;

    /**
     * Determines whether the Job should be presented
     */
    private boolean isVisible;

    /**
     * The start time of the Job
     */
    private Date startTime;

    /**
     * The end time of the Job
     */
    private Date endTime;

    /**
     * Describes when the Job was last updated
     */
    private Date lastUpdateTime;

    /**
     * A pass-thru string to identify one or more Jobs cross-layer
     */
    private String correlationId;

    /**
     * A flag defining if this Job were invoked from external plug-in
     */
    private boolean external;

    /**
     * A flag indicating if the Job is auto cleared from the table after the configured time for succeeded/failed jobs
     */
    private boolean autoCleared;

    /**
     * A collection which holds the entities associated with the Job
     */
    private Map<Guid, VdcObjectType> jobSubjectEntities;

    /**
     * A collection which stores the steps of the Job
     */
    private List<Step> steps;

    /**
     * Define if the Job should be ended when the {@code CommandBase.executeAction()} ends
     */
    private transient boolean isAsyncJob;

    /**
     * Maximum job weight
     */
    public static final int MAX_WEIGHT = 100;

    public Job() {
        status = JobExecutionStatus.STARTED;
        autoCleared = true;
        isVisible = true;
        steps = new ArrayList<>();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> list) {
        this.steps = list;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        setLastUpdateTime(startTime);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setIsAsyncJob(boolean isAsyncJob) {
        this.isAsyncJob = isAsyncJob;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean isExternal) {
        this.external = isExternal;
    }

    public boolean isAutoCleared() {
        return autoCleared;
    }

    public void setAutoCleared(boolean isAutoCleared) {
        this.autoCleared = isAutoCleared;
    }

    public boolean isAsyncJob() {
        return isAsyncJob;
    }

    public void setJobSubjectEntities(Map<Guid, VdcObjectType> jobEntities) {
        this.jobSubjectEntities = jobEntities;
    }

    public Map<Guid, VdcObjectType> getJobSubjectEntities() {
        return jobSubjectEntities;
    }

    public void setOwnerId(Guid ownerId) {
        this.ownerId = ownerId;
    }

    public Guid getOwnerId() {
        return ownerId;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        setLastUpdateTime(endTime);
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public long getEngineSessionSeqId() {
        return engineSessionSeqId;
    }

    public void setEngineSessionSeqId(long engineSessionSeqId) {
        this.engineSessionSeqId = engineSessionSeqId;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    public Step addStep(Step step) {
        lastUpdateTime = new Date();
        step.setJobId(id);
        step.setStepNumber(steps.size());
        step.setCorrelationId(correlationId);
        steps.add(step);
        return step;
    }

    public Step addStep(StepEnum stepType, String description) {
        return addStep(new Step(stepType, description));
    }

    public Step getStep(StepEnum stepType) {
        Step stepByType = null;
        for (Step step : steps) {
            if (step.getStepType() == stepType) {
                stepByType = step;
                break;
            }
        }
        return stepByType;
    }

    public Step getDirectStep(StepEnum stepType) {
        Step stepByType = null;
        for (Step step : steps) {
            // A direct step is a step that has no parent step
            if (step.getStepType() == stepType && step.getParentStepId() == null) {
                stepByType = step;
                break;
            }
        }
        return stepByType;
    }

    public void markJobEnded(boolean result) {
        if (status == JobExecutionStatus.STARTED) {
            endTime = lastUpdateTime = new Date();

            if (result) {
                status = JobExecutionStatus.FINISHED;
            } else {
                status = JobExecutionStatus.FAILED;
            }
        }
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                actionType,
                engineSessionSeqId,
                correlationId,
                endTime,
                id,
                isVisible,
                jobSubjectEntities,
                lastUpdateTime,
                ownerId,
                startTime,
                status,
                steps,
                external,
                autoCleared
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Job)) {
            return false;
        }
        Job other = (Job) obj;
        return actionType == other.actionType
                && engineSessionSeqId == other.engineSessionSeqId
                && Objects.equals(correlationId, other.correlationId)
                && Objects.equals(endTime, other.endTime)
                && Objects.equals(id, other.id)
                && isVisible == other.isVisible
                && Objects.equals(jobSubjectEntities, other.jobSubjectEntities)
                && Objects.equals(lastUpdateTime, other.lastUpdateTime)
                && Objects.equals(ownerId, other.ownerId)
                && Objects.equals(startTime, other.startTime)
                && status == other.status
                && Objects.equals(steps, other.steps)
                && external == other.external
                && autoCleared == other.autoCleared;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("actionType", actionType)
                .append("status", status)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .append("lastUpdateTime", lastUpdateTime)
                .append("id", id)
                .append("correlationId", correlationId)
                .append("engineSessionSeqId", engineSessionSeqId)
                .append("description", description)
                .append("visible", isVisible)
                .append("jobSubjectEntities", jobSubjectEntities)
                .append("ownerId", ownerId)
                .append("steps", steps)
                .append("external", external)
                .append("autoCleared", autoCleared)
                .toString();
    }
}
