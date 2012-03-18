package org.ovirt.engine.core.dal.job;

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
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Implements the CRUD operations for the Job entities.
 */
public class JobRepositoryImpl implements JobRepository {

    private static Log log = LogFactory.getLog(JobRepositoryImpl.class);

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
                    stepDao.save(step);
                    jobDao.updateJobLastUpdateTime(step.getJobId(), new Date());
                } catch (Exception e) {
                    log.errorFormat("Failed to save step {0}, {1}.", step.getId(), step.getStepName(), e);
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
                    stepDao.update(step);
                    jobDao.updateJobLastUpdateTime(step.getJobId(), new Date());
                } catch (Exception e) {
                    log.errorFormat("Failed to update step {0}, {1}.", step.getId(), step.getStepName(), e);
                }
                return null;
            }
        });
    }

    @Override
    public void saveJob(final Job job) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                jobDao.save(job);
                Set<Entry<Guid, VdcObjectType>> entrySet = job.getJobSubjectEntities().entrySet();
                for (Entry<Guid, VdcObjectType> entry : entrySet) {
                    jobSubjectEntityDao.save(job.getId(), entry.getKey(), entry.getValue());
                }
                return null;
            }
        });
    }

    @Override
    public void updateJob(final Job job) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                jobDao.update(job);
                return null;
            }
        });
    }

    @Override
    public void updateJobLastUpdateTime(final Guid jobId, final Date lastUpdateTime) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                jobDao.updateJobLastUpdateTime(jobId, lastUpdateTime);
                return null;
            }
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

            List<Step> steps = stepDao.getStepsByJobId(jobId);
            if (!steps.isEmpty()) {
                job.setSteps(buildStepsTree(steps));
            }
        }
        return job;
    }

    /**
     * Gets a list of {@link Step} entities ordered by:
     * <li> parent step id, preceded by nulls
     * <li> step number
     * @param steps
     * @return a collection of the steps.
     */
    private List<Step> buildStepsTree(List<Step> steps) {
        List<Step> jobDirectSteps = new ArrayList<Step>();

        // a map of parent step id and a list of child-steps
        Map<NGuid, List<Step>> parentStepMap = new HashMap<NGuid, List<Step>>();

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
        List<Job> jobList = new ArrayList<Job>();
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
                stepDao.update(existingStep);
                stepDao.save(newStep);
                jobDao.updateJobLastUpdateTime(existingStep.getJobId(), new Date());
                return null;
            }
        });
    }

    @Override
    public void updateCompletedJobAndSteps(final Job job) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                stepDao.updateJobStepsCompleted(job.getId(), job.getStatus(), job.getEndTime());
                jobDao.update(job);
                return null;
            }
        });
    }

    @Override
    public void finalizeJobs() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
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
