package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;

public interface StepDao extends GenericDao<Step, Guid> {

    /**
     * Check if the {@link Step} with the given id exists or not.
     *
     * @param id
     *            The step's id.
     * @return Does the step exist or not.
     */
    boolean exists(Guid id);

    /**
     * Retrieve all the entities of type {@link Step} for a specific {@code Job}.
     *
     * @return A list of all the job's steps in a flat collection, or an empty list if none is found.
     */
    List<Step> getStepsByJobId(Guid jobId);

    /**
     * Retrieve all the entities of type {@link Step} by a specific step-parent-id.
     *
     * @return A list of all the parent-sub-steps in a flat collection, or an empty list if none is found.
     */
    List<Step> getStepsByParentStepId(Guid parentStepId);

    /**
     * Updates the steps associated with a given job ID, is the current status of the steps differ from the given status
     * or from {@code ExecutionStatus.STARTED}
     *
     * @param jobId
     *            The id of the job which the steps associated with.
     * @param status
     *            The status to update, other than {@code ExecutionStatus.STARTED}
     * @param endTime
     *            The time when the step is ended.
     */
    void updateJobStepsCompleted(Guid jobId, JobExecutionStatus status, Date endTime);

    /**
     * Updates the step progress
     */
    void updateStepProgress(Guid stepId, Integer progress);

    /**
     * Retrieve all steps associated with the given external id
     */
    List<Step> getStepsByExternalId(Guid externalId);

    /**
     * Retrieve all external ids for steps that are not yet completed
     */
    List<Guid> getExternalIdsForRunningSteps(ExternalSystemType systemType);

    /**
     * Retrieves all {@link Step} in status {@code JobExecutionStatus.STARTED} for the given {@link SubjectEntity}
     */
    List<Step> getStartedStepsByStepSubjectEntity(SubjectEntity subjectEntity);
 }

