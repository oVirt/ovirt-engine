package org.ovirt.engine.core.common.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents the Job entity which encapsulates a client action in the system. The Job contains a collection
 * of steps which describe portions of the entire Job. The Job entity is capable to produce a descriptive tree of
 * steps, reflecting the action parts.
 */
public class Job implements IVdcQueryable, BusinessEntity<Guid> {

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
    private VdcActionType actionType;

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
     * Define if the Job should be ended when the {@Code CommandBase.ExecuteAction()} ends
     */
    transient private boolean isAsyncJob;

    public Job() {
        status = JobExecutionStatus.STARTED;
        autoCleared = true;
        isVisible = true;
        steps = new ArrayList<Step>();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> list) {
        this.steps = list;
    }

    public void setActionType(VdcActionType actionType) {
        this.actionType = actionType;
    }

    public VdcActionType getActionType() {
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

    /**
     * Returns the last step on top level hierarchy.
     *
     * @return The last added step or <code>null</code> if list of steps is empty.
     */
    public Step getLastStep() {
        if (steps.isEmpty()) {
            return null;
        }
        return steps.get(steps.size() - 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionType == null) ? 0 : actionType.hashCode());
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isVisible ? 1231 : 1237);
        result = prime * result + ((jobSubjectEntities == null) ? 0 : jobSubjectEntities.hashCode());
        result = prime * result + ((lastUpdateTime == null) ? 0 : lastUpdateTime.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((steps == null) ? 0 : steps.hashCode());
        result = prime * result + (external ? 1231 : 1237);
        result = prime * result + (autoCleared ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Job)) {
            return false;
        }
        Job other = (Job) obj;
        if (actionType != other.actionType) {
            return false;
        }
        if (correlationId == null) {
            if (other.correlationId != null) {
                return false;
            }
        } else if (!correlationId.equals(other.correlationId)) {
            return false;
        }
        if (endTime == null) {
            if (other.endTime != null) {
                return false;
            }
        } else if (!endTime.equals(other.endTime)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (isVisible != other.isVisible) {
            return false;
        }
        if (jobSubjectEntities == null) {
            if (other.jobSubjectEntities != null) {
                return false;
            }
        } else if (!jobSubjectEntities.equals(other.jobSubjectEntities)) {
            return false;
        }
        if (lastUpdateTime == null) {
            if (other.lastUpdateTime != null) {
                return false;
            }
        } else if (!lastUpdateTime.equals(other.lastUpdateTime)) {
            return false;
        }
        if (ownerId == null) {
            if (other.ownerId != null) {
                return false;
            }
        } else if (!ownerId.equals(other.ownerId)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (steps == null) {
            if (other.steps != null) {
                return false;
            }
        } else if (!steps.equals(other.steps)) {
            return false;
        }
        if (external != other.external) {
            return false;
        }
        if (autoCleared != other.autoCleared) {
            return false;
        }
        return true;
    }
}
