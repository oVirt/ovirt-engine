package org.ovirt.engine.core.bll.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
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
        TransactionSupport.executeInNewTransaction(() -> {
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
        });
    }

    @Override
    public void updateStep(final Step step) {
        TransactionSupport.executeInNewTransaction(() -> {

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
        });
    }

    @Override
    public void saveJob(final Job job) {
        TransactionSupport.executeInNewTransaction(() -> {
            jobDao.save(job);
            Set<Entry<Guid, VdcObjectType>> entrySet = job.getJobSubjectEntities().entrySet();
            for (Entry<Guid, VdcObjectType> entry : entrySet) {
                jobSubjectEntityDao.save(job.getId(), entry.getKey(), entry.getValue());
            }
            return null;
        });
    }

    @Override
    public Job getJob(final Guid jobId) {
        Job job = jobDao.get(jobId);
        if (job != null) {
            Map<Guid, VdcObjectType> jobSubjectEntity =
                    jobSubjectEntityDao.getJobSubjectEntityByJobId(jobId);
            job.setJobSubjectEntities(jobSubjectEntity);
        }
        return job;
    }

    @Override
    public Job getJobWithSteps(final Guid jobId) {
        Job job = jobDao.get(jobId);
        if (job != null) {
            Map<Guid, VdcObjectType> jobSubjectEntity =
                    jobSubjectEntityDao.getJobSubjectEntityByJobId(jobId);
            job.setJobSubjectEntities(jobSubjectEntity);
            loadJobSteps(job);
        }
        return job;
    }

    @Override
    public void loadJobSteps(final Job job) {
        List<Step> steps = stepDao.getStepsByJobId(job.getId());
        if (!steps.isEmpty()) {
            job.setSteps(buildStepsTree(steps));
        }
    }

    @Override
    public void loadParentStepSteps(final Step step) {
        List<Step> steps = stepDao.getStepsByParentStepId(step.getId());
        if (!steps.isEmpty()) {
            step.setSteps(steps);
        }
    }


    /**
     * Gets a list of {@link Step} entities ordered by:
     * <li> parent step id, preceded by nulls
     * <li> step number
     * @return a collection of the steps.
     */
    private List<Step> buildStepsTree(List<Step> steps) {
        List<Step> jobDirectSteps = new ArrayList<>();

        // a map of parent step id and a list of child-steps
        Map<Guid, List<Step>> parentStepMap = new HashMap<>();

        for (Step step : steps) {
            if (step.getParentStepId() == null) {
                jobDirectSteps.add(step);
            } else {
                MultiValueMapUtils.addToMap(step.getParentStepId(), step, parentStepMap);
            }
        }

        for (Step step : steps) {
            step.setSteps(parentStepMap.get(step.getId()));
        }
        return jobDirectSteps;
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
        TransactionSupport.executeInNewTransaction(() -> {
            jobDao.updateJobLastUpdateTime(existingStep.getJobId(), new Date());
            stepDao.update(existingStep);
            stepDao.save(newStep);
            return null;
        });
    }

    @Override
    public void updateCompletedJobAndSteps(final Job job) {
        TransactionSupport.executeInNewTransaction(() -> {
            jobDao.update(job);
            stepDao.updateJobStepsCompleted(job.getId(), job.getStatus(), job.getEndTime());
            return null;
        });
    }

    @Override
    public void closeCompletedJobSteps(final Guid jobId, final JobExecutionStatus status) {
        TransactionSupport.executeInNewTransaction(() -> {
            stepDao.updateJobStepsCompleted(jobId, status, new Date());
            return null;
        });
    }

    @Override
    public void finalizeJobs() {
        TransactionSupport.executeInNewTransaction(() -> {
            jobDao.deleteRunningJobsOfTasklessCommands();
            jobDao.updateStartedExecutionEntitiesToUnknown(new Date());
            return null;
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
