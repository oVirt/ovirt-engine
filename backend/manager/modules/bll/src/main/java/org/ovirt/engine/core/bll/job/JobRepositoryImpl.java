package org.ovirt.engine.core.bll.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the CRUD operations for the Job entities.
 */
public class JobRepositoryImpl implements JobRepository {

    private static final Logger log = LoggerFactory.getLogger(JobRepositoryImpl.class);

    private JobDao jobDao;
    private JobSubjectEntityDao jobSubjectEntityDao;
    private StepDao stepDao;

    public JobRepositoryImpl() {
    }

    public JobRepositoryImpl(JobDao jobDao, JobSubjectEntityDao jobSubjectEntityDao, StepDao stepDao) {
        setJobDao(jobDao);
        setJobSubjectEntityDao(jobSubjectEntityDao);
        setStepDao(stepDao);
    }

    @Override
    public void saveStep(final Step step) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                try {
                    jobDao.updateJobLastUpdateTime(step.getJobId(), new Date());
                    stepDao.save(step);
                } catch (Exception e) {
                    log.error("Failed to save step '{}', '{}': {}",
                            step.getId(),
                            step.getStepName(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
                return null;
            }
        });
    }

    @Override
    public void updateStep(final Step step) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {

                try {
                    jobDao.updateJobLastUpdateTime(step.getJobId(), new Date());
                    stepDao.update(step);
                } catch (Exception e) {
                    log.error("Failed to update step '{}', '{}': {}",
                            step.getId(),
                            step.getStepName(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
                return null;
            }
        });
    }

    @Override
    public void saveJob(final Job job) {
        jobDao.save(job);
    }

    @Override
    public Job getJob(final Guid jobId) {
        return TransactionSupport.executeInNewTransaction(new TransactionMethod<Job>() {

            @Override
            public Job runInTransaction() {
                Job job = jobDao.get(jobId);
                if (job != null) {
                    // This loads the lazy collection
                    job.getJobSubjectEntities();
                }
                return job;
            }
        });
    }

    @Override
    public void loadParentStepSteps(final Step step) {
        List<Step> steps = stepDao.getStepsByParentStepId(step.getId());
        if (!steps.isEmpty()) {
            step.setSteps(steps);
        }
    }

    @Override
    public Step getStep(Guid stepId) {
        return stepDao.get(stepId);
    }

    @Override
    public List<Job> getJobsByEntityAndAction(Guid entityId, VdcActionType actionType) {
        List<Job> jobList = new ArrayList<>();
        List<Guid> jobIdsList = jobSubjectEntityDao.getJobIdByEntityId(entityId);

        for (Guid jobId : jobIdsList) {
            Job job = jobDao.get(jobId);
            if (job != null && job.getActionType() == actionType) {
                jobList.add(job);
            }
        }
        return jobList;
    }

    @Override
    public void updateExistingStepAndSaveNewStep(final Step existingStep, final Step newStep) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                jobDao.updateJobLastUpdateTime(existingStep.getJobId(), new Date());
                stepDao.update(existingStep);
                stepDao.save(newStep);
                return null;
            }
        });
    }

    @Override
    public void updateCompletedJobAndSteps(final Job job) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (Step step : job.getSteps()) {
                    if (step.getStatus() == JobExecutionStatus.STARTED) {
                        step.setStatus(job.getStatus());
                        step.setEndTime(job.getEndTime());
                    }
                }
                jobDao.update(job);
                return null;
            }
        });
    }

    @Override
    public void closeCompletedJobSteps(final Guid jobId, final JobExecutionStatus status) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                stepDao.updateJobStepsCompleted(jobId, status, new Date());
                return null;
            }
        });
    }

    @Override
    public void finalizeJobs() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                jobDao.deleteRunningJobsOfTasklessCommands();
                jobDao.updateStartedExecutionEntitiesToUnknown(new Date());
                return null;
            }
        });

    }

    public void setJobDao(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    public void setJobSubjectEntityDao(JobSubjectEntityDao jobSubjectEntityDao) {
        this.jobSubjectEntityDao = jobSubjectEntityDao;
    }

    public void setStepDao(StepDao stepDao) {
        this.stepDao = stepDao;
    }

}
