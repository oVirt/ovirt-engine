package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;

public interface JobDao extends GenericDao<Job, Guid>, SearchDao<Job> {

    /**
     * Checks if the {@link Job} with the given id exists or not.
     *
     * @param id
     *            The job's id.
     * @return Does the job exist or not.
     */
    boolean exists(Guid id);

    /**
     * Retrieves a configured page of Jobs start from a given offset
     *
     * @param offset
     *            the offset to fetch the Jobs from
     * @param pageSize
     *            the quantity of Jobs to fetch
     * @return a collection of jobs
     */
    List<Job> getJobsByOffsetAndPageSize(int offset, int pageSize);

    /**
     * Retrieves a list of Jobs by a given correlation-ID
     *
     * @param correlationId
     *            the correlation-ID to fetch Jobs by
     * @return a collection of jobs
     */
    List<Job> getJobsByCorrelationId(String correlationId);

    /**
     * Retrieves a list of Jobs by a given engine-session-seq-ID and job execution status
     *
     * @param engineSessionSeqId
     *            the engine-session-seq-ID to fetch Jobs by
     * @param status
     *            the job execution status to fetch Jobs by
     * @return a collection of jobs
     */
    List<Job> getJobsBySessionSeqIdAndStatus(long engineSessionSeqId, JobExecutionStatus status);

    /**
     * Updates {@link Job} entity with the last update time of a given instance
     *
     * @param jobId
     *            the id of the job instance which should be updated
     * @param lastUpdateTime
     *            the last date when the Job was modified
     */
    void updateJobLastUpdateTime(Guid jobId, Date lastUpdateTime);

    /**
     * Deletes job entities which their end time is older than a given date and their status
     * appears in the provided list of statuses.
     *
     * @param sinceDate
     *            the date to delete jobs older than
     * @param statusesList
     *            a comma separated list of statuses {@link JobExecutionStatus}
     */
    void deleteJobOlderThanDateWithStatus(Date sinceDate, List<JobExecutionStatus> statusesList);

    /**
     * Updates {@link Job} and {@link Step} entries with status {@link JobExecutionStatus#STARTED} to
     * {@link JobExecutionStatus#UNKNOWN} for {@link Job} without external tasks.
     *
     * @param updateTime
     *            The update time to set for {@link Job} end time and last update time and for {@link Step} end time.
     */
    void updateStartedExecutionEntitiesToUnknown(Date updateTime);

    /**
     * Deletes completed jobs.
     * Successful jobs have {@link JobExecutionStatus#FINISHED} status.
     * Failed jobs have one of the following statuses:
     * {@link JobExecutionStatus#FAILED}, {@link JobExecutionStatus#ABORTED}, {@link JobExecutionStatus#UNKNOWN}.
     *
     * @param succeededJobs
     *            all successful jobs having older end time than this date will be deleted.
     * @param failedJobs
     *            all failed jobs having older end time than this date will be deleted.
     */
    void deleteCompletedJobs(Date succeededJobs, Date failedJobs);

    /**
     * Checks if a job has step associated with VDSM task.
     *
     * @param jobId
     *            The job id to search by
     * @return true if the job contains a step associated with VDSM task, else false
     */
    boolean checkIfJobHasTasks(Guid jobId);

    void deleteRunningJobsOfTasklessCommands();
}
