package org.ovirt.engine.core.common.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * represents a meaningful phase of the Job. A Step could be a parent of other steps (e.g. step named EXECUTION could
 * have a list of steps beneath it which are also part of the job)
 */
public class Step implements Queryable, BusinessEntity<Guid> {

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 3711656756401350600L;

    /**
     * Maximum step progress
     */
    public static final int MAX_PROGRESS = 100;

    /**
     * Minimum step progress
     */
    public static final int MIN_PROGRESS = 0;

    /**
     * The Step ID uniquely identifies a disk in the system.
     */
    private Guid id;

    /**
     * The job which the step comprises
     */
    private Guid jobId;

    /**
     * The direct parent step of the current step
     */
    private Guid parentStepId;

    /**
     * The step type
     */
    private StepEnum stepType;

    /**
     * The description of the step
     */
    private String description;

    /**
     * The order of the step in current hierarchy level
     */
    private int stepNumber;

    /**
     * The status of the step
     */
    private JobExecutionStatus status;

    /**
     * The start time of the step
     */
    private Date startTime;

    /**
     * The end time of the step
     */
    private Date endTime;

    /**
     * A pass-thru string to identify this step as part of a wider action
     */
    private String correlationId;

    /**
     * A flag defining if this step were invoked from external plug-in
     */
    private boolean external;

    /**
     * An external system referenced by the step (e.g. VDSM)
     */
    private ExternalSystem externalSystem;

    /**
     * A collection which holds the entities associated with the Step
     */
    private List<StepSubjectEntity> subjectEntities;

    private Integer progress;

    /**
     * The successors steps
     */
    private List<Step> steps;

    public Step() {
        status = JobExecutionStatus.STARTED;
        externalSystem = new ExternalSystem();
        steps = new ArrayList<>();
    }

    public Step(StepEnum stepType) {
        this.id = Guid.newGuid();
        this.parentStepId = null;
        this.stepType = stepType;
        this.startTime = new Date();
        status = JobExecutionStatus.STARTED;
        externalSystem = new ExternalSystem();
        steps = new ArrayList<>();
    }

    public Step(StepEnum stepType, String description) {
        this(stepType);
        if (description != null) {
            setDescription(description);
        } else {
            setDescription(getStepName());
        }
    }

    public StepEnum getStepType() {
        return stepType;
    }

    public void setStepType(StepEnum stepType) {
        this.stepType = stepType;
    }

    public String getStepName() {
        return stepType.name();
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setParentStepId(Guid parentStepId) {
        this.parentStepId = parentStepId;
    }

    public Guid getParentStepId() {
        return parentStepId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setExternalSystem(ExternalSystem externalSystem) {
        this.externalSystem = externalSystem;
    }

    public ExternalSystem getExternalSystem() {
        return externalSystem;
    }

    public void setStepNumber(int position) {
        this.stepNumber = position;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean isExternal) {
        this.external = isExternal;
    }

    public Step addStep(StepEnum childStepType, String description) {
        Step childStep = new Step(childStepType);
        childStep.setParentStepId(id);
        childStep.setJobId(jobId);
        childStep.setStepNumber(getSteps().size());
        childStep.setCorrelationId(correlationId);

        if (description == null) {
            childStep.setDescription(childStep.getStepName());
        } else {
            childStep.setDescription(description);
        }
        steps.add(childStep);
        return childStep;
    }

    public void markStepEnded(boolean isSuccess) {
        endTime = new Date();
        if (isSuccess) {
            setStatus(JobExecutionStatus.FINISHED);
        } else {
            setStatus(JobExecutionStatus.FAILED);
        }
    }

    /**
     * Set completion status to the step which its current status isn't completed yet. The step status won't be changed
     * if the status to update matches the current status.
     *
     * @param exitStatus
     *            The completion status of the step
     */
    public void markStepEnded(JobExecutionStatus exitStatus) {
        if (status == JobExecutionStatus.STARTED || status == JobExecutionStatus.UNKNOWN) {
            if (exitStatus != JobExecutionStatus.STARTED && status != exitStatus) {
                status = exitStatus;
                endTime = new Date();
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

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<StepSubjectEntity> getSubjectEntities() {
        return subjectEntities;
    }

    public void setSubjectEntities(List<StepSubjectEntity> subjectEntities) {
        this.subjectEntities = subjectEntities;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
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

    @Override
    public int hashCode() {
        return Objects.hash(
                correlationId,
                description,
                endTime,
                externalSystem,
                id,
                jobId,
                parentStepId,
                startTime,
                status,
                stepNumber,
                stepType,
                steps,
                external
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Step)) {
            return false;
        }
        Step other = (Step) obj;
        return Objects.equals(correlationId, other.correlationId)
                && Objects.equals(description, other.description)
                && Objects.equals(endTime, other.endTime)
                && Objects.equals(externalSystem, other.externalSystem)
                && Objects.equals(id, other.id)
                && Objects.equals(jobId, other.jobId)
                && Objects.equals(parentStepId, other.parentStepId)
                && Objects.equals(startTime, other.startTime)
                && status == other.status
                && stepNumber == other.stepNumber
                && stepType == other.stepType
                && Objects.equals(steps, other.steps)
                && external == other.external;
    }

}
