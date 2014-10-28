package org.ovirt.engine.core.common.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * represents a meaningful phase of the Job. A Step could be a parent of other steps (e.g. step named EXECUTION could
 * have a list of steps beneath it which are also part of the job)
 */
@Entity
@Table(name = "step")
@Cacheable(true)
@NamedQueries({
        @NamedQuery(
                name = "Step.updateJobStepsCompleted",
                query = "update Step s "
                        + "set s.status = :status, s.endTime = :endTime "
                        + "where s.status = :startedStatus and s.jobId = :jobId"),
        @NamedQuery(
                name = "Step.getStepsByExternalId",
                query = "select s "
                        + "from Step s "
                        + "where s.externalSystem.externalId = :externalId "
                        + "order by s.parentStepId, s.stepNumber"),
        @NamedQuery(
                name = "Step.getExternalIdsForRunningSteps",
                query = "select s.externalSystem.externalId "
                        + "from Job j inner join j.steps s "
                        + "where j.status = :status and s.externalSystem.externalSystemType = :type"),
        @NamedQuery(
                name = "Step.getStepsByJobIdForVdsmAndGluster",
                query = "select s "
                        + "from Step s "
                        + "where s.jobId = :jobId "
                        + "and s.externalSystem.externalId is not null "
                        + "and s.externalSystem.externalSystemType in :systemType")
})

public class Step extends IVdcQueryable implements BusinessEntity<Guid> {

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 3711656756401350600L;

    /**
     * The Step ID uniquely identifies a disk in the system.
     */
    @Id
    @Column(name = "step_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    /**
     * The job which the step comprises
     */
    @Column(name = "job_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid jobId;

    /**
     * The direct parent step of the current step
     */
    @Column(name = "parent_step_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid parentStepId;

    /**
     * The step type
     */
    @Column(name = "step_type")
    @Enumerated(EnumType.STRING)
    private StepEnum stepType;

    /**
     * The description of the step
     */
    @Column(name = "description")
    private String description;

    /**
     * The order of the step in current hierarchy level
     */
    @Column(name = "step_number")
    private int stepNumber;

    /**
     * The status of the step
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobExecutionStatus status;

    /**
     * The start time of the step
     */
    @Column(name = "start_time")
    private Date startTime;

    /**
     * The end time of the step
     */
    @Column(name = "end_time")
    private Date endTime;

    /**
     * A pass-thru string to identify this step as part of a wider action
     */
    @Column(name = "correlation_id")
    private String correlationId;

    /**
     * A flag defining if this step were invoked from external plug-in
     */
    @Column(name = "is_external")
    private boolean external;

    /**
     * An external system referenced by the step (e.g. VDSM)
     */
    @Embedded
    private ExternalSystem externalSystem;

    /**
     * The successors steps
     */
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parentStepId", orphanRemoval = true)
    @OrderBy("stepNumber")
    private List<Step> steps;

    public Step() {
        status = JobExecutionStatus.STARTED;
        externalSystem = new ExternalSystem();
        steps = new ArrayList<Step>();
    }

    public Step(StepEnum stepType) {
        this.id = Guid.newGuid();
        this.parentStepId = null;
        this.stepType = stepType;
        this.startTime = new Date();
        status = JobExecutionStatus.STARTED;
        externalSystem = new ExternalSystem();
        steps = new ArrayList<Step>();
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
        return Objects.hashCode(id);
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
        return Objects.equals(id, other.id);
    }

}
