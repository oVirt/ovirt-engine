package org.ovirt.engine.core.bll.job;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents basic CRUD operations for Job and Step objects.
 * Each modification operation is being executed in a new transaction.
 */
public interface JobRepository {

    /**
     * Persists a new instance of {@link Step} the entity and update the {@link Job} modification date.
     *
     * @param step
     *            The {@link Step} entity to persist (can't be <code>null</code>).
     */
    void saveStep(Step step);

    /**
     * Persists a new instance of {@link Step} entity with the provided {@link StepSubjectEntity} list and updates the
     * {@link Job} modification date.
     *
     * @param step
     *            The {@link Step} entity to persist (can't be <code>null</code>).
     * @param stepSubjectEntities
     *            The {@link StepSubjectEntity} list to persist (can't be <code>null</code>).
     */
    void saveStep(Step step, Collection<StepSubjectEntity> stepSubjectEntities);

    /**
     * Updates an existing {@link Step} entity with data from the given instance.
     *
     * @param step
     *            The {@link Step} instance, containing data to update (can't be <code>null</code>).
     */
    void updateStep(Step step);

    /**
     * Persists a new instance of {@link Job} the entity.
     *
     * @param job
     *            The {@link Job} entity to persist (can't be <code>null</code>).
     */
    void saveJob(Job job);

    /**
     * Retrieves the {@link Job} entity with the given id.
     *
     * @param id
     *            The id to look by (can't be <code>null</code>).
     * @return The entity instance, or <code>null</code> if not found.
     */
    Job getJob(Guid jobId);

    /**
     * Retrieves the {@link Step} entity with the given id.
     *
     * @param id
     *            The id to look by (can't be <code>null</code>).
     * @param loadSubjectEntities
     *            boolean indicating whether to load the step's {@link StepSubjectEntity} list
     * @return The entity instance, or <code>null</code> if not found.
     */
    Step getStep(Guid stepId, boolean loadSubjectEntities);

    /**
     * Retrieves a list of {@link Job} entities with the given characteristics.
     *
     * @param entityId
     *            The id of the entity which is associated with the Job via {@link JobSujectEntity}
     * @param actionType
     *            The action type which is associated with the job.
     * @return a list of {@link Job} if found a match or an empty list.
     */
    List<Job> getJobsByEntityAndAction(Guid entityId, ActionType actionType);

    /**
     * Retrieves the {@link Job} entity with the given id, populated with its Steps.
     *
     * @param jobId
     *            The id to look by (can't be <code>null</code>).
     * @return The entity instance, or <code>null</code> if not found.
     */
    Job getJobWithSteps(Guid jobId);

    /**
     * Update the {@link Job} entity with its Steps.
     *
     * @param job
     *            The job to update with its steps(can't be <code>null</code>).
     */
    void loadJobSteps(Job job);

    /**
     * Update the {@link Step} entity with its Steps.
     *
     * @param step
     *            The job to update with its steps(can't be <code>null</code>).
     */
    void loadParentStepSteps(Step step);

    /**
     * Handles the status of uncompleted jobs and their steps:
     * <li>Job without Steps that have tasks will be marked as {@code ExecutionStatus.UNKNOWN}
     * <li>Job with Steps that have tasks will be remained as is to be process by the {@code AsyncTaskManager}.
     */
    void finalizeJobs();

    /**
     * Updates existing step and saving the step after in the same transaction. Should be used when two steps followed
     * each other, e.g. EXECUTING step is completed and right after creating the FINALIZING step.
     *
     * @param existingStep
     *            The step which should be updated.
     * @param newStep
     *            The step which should be created.
     */
    void updateExistingStepAndSaveNewStep(Step existingStep, Step newStep);

    /**
     * Updates the {@code Job} entity and its steps as completed with the given status.
     *
     * @param job
     *            The job to end.
     */
    void updateCompletedJobAndSteps(Job job);

    void closeCompletedJobSteps(Guid jobId, JobExecutionStatus status);

}
