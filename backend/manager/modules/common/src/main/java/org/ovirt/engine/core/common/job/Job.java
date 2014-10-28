package org.ovirt.engine.core.common.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
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
@Entity
@Table(name = "job")
@Cacheable(true)
@NamedQueries({
        @NamedQuery(name = "Job.getJobsByCorrelationId",
                query = "select j from Job j where j.correlationId = :correlationId"),
        @NamedQuery(name = "Job.getJobsByOffsetAndPageSize",
                query = "select j from Job j where j.status in (:status) order by lastUpdateTime DESC"),
        @NamedQuery(name = "Job.getJobsByOffsetAndPageSizeNotInStatus",
                query = "select j from Job j where j.status not in (:status) order by lastUpdateTime DESC"),
        @NamedQuery(
                name = "Job.deleteCompletedJobs",
                query = "delete from Job j where j.autoCleared = true "
                        + "and ((j.endTime < :failedEndTime and j.status in (:failStatus)) "
                        + "or (j.endTime < :successEndTime and j.status = :successStatus))"),
        @NamedQuery(
                name = "Job.deleteJobOlderThanDateWithStatus",
                query = "delete from Job j where j.autoCleared = true and j.endTime < :sinceDate "
                        + "and j.status in (:statuses)")
})
public class Job extends IVdcQueryable implements BusinessEntity<Guid> {

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 9003809286645635497L;

    /**
     * The Job ID uniquely identifies a disk in the system.
     */

    @Id
    @Column(name = "job_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    /**
     * The action type which the Job describes
     */
    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private VdcActionType actionType;

    /**
     * The description of the job
     */
    @Column(name = "description")
    private String description;

    /**
     * The status of the job
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobExecutionStatus status;

    /**
     * The user id which invoked the job
     */
    @Column(name = "owner_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid ownerId;

    /**
     * Determines whether the Job should be presented
     */
    @Column(name = "visible")
    private boolean isVisible;

    /**
     * The start time of the Job
     */
    @Column(name = "start_time")
    private Date startTime;

    /**
     * The end time of the Job
     */
    @Column(name = "end_time")
    private Date endTime;

    /**
     * Describes when the Job was last updated
     */
    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    /**
     * A pass-thru string to identify one or more Jobs cross-layer
     */
    @Column(name = "correlation_id")
    private String correlationId;

    /**
     * A flag defining if this Job were invoked from external plug-in
     */
    @Column(name = "is_external")
    private boolean external;

    /**
     * A flag indicating if the Job is auto cleared from the table after the configured time for succeeded/failed jobs
     */
    @Column(name = "is_auto_cleared")
    private boolean autoCleared;

    /**
     * A collection which holds the entities associated with the Job
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @MapKey(name = "entity_id")
    @CollectionTable(schema = "jpa", name = "job_subject_entity",
            joinColumns = @JoinColumn(name = "job_id"))
    private transient Map<Guid, VdcObjectType> jobSubjectEntities;

    /**
     * A collection which stores the steps of the Job
     */

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "jobId", orphanRemoval = true)
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
        return Objects.hashCode(id);
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
        return Objects.equals(id, other.id);
    }
}
