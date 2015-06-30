package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.dao.jpa.TransactionalInterceptor;
import org.ovirt.engine.core.utils.transaction.Transactional;
import org.springframework.stereotype.Component;

@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class JobDaoImpl extends AbstractJpaDao<Job, Guid> implements JobDao {

    @Inject
    private StepDao stepDao;

    protected JobDaoImpl() {
        super(Job.class);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Guid id) {
        return get(id) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsByOffsetAndPageSize(int offset, int pageSize) {
        List<Job> allJobs =
                multipleResults(getEntityManager().createNamedQuery("Job.getJobsByOffsetAndPageSize", Job.class)
                        .setParameter("status",
                        JobExecutionStatus.STARTED));
        allJobs.addAll(multipleResults(getEntityManager().createNamedQuery("Job.getJobsByOffsetAndPageSizeNotInStatus",
                Job.class)
                .setParameter("status", EnumSet.of(JobExecutionStatus.STARTED,
                        JobExecutionStatus.UNKNOWN))));
        int endIndex = Math.min(offset + pageSize, allJobs.size());
        return allJobs.subList(offset, endIndex);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsByCorrelationId(String correlationId) {
        return multipleResults(getEntityManager().createNamedQuery("Job.getJobsByCorrelationId", Job.class)
                .setParameter("correlationId", correlationId));
    }

    @Override
    public void updateJobLastUpdateTime(Guid jobId, Date lastUpdateTime) {
        Job job = get(jobId);
        job.setLastUpdateTime(lastUpdateTime);
        update(job);
    }

    @Override
    public void deleteJobOlderThanDateWithStatus(Date sinceDate, List<JobExecutionStatus> statusesList) {
        updateQuery(getEntityManager().createNamedQuery("Job.deleteJobOlderThanDateWithStatus")
                .setParameter("sinceDate", sinceDate)
                .setParameter("statuses", statusesList));
    }

    @Override
    public void updateStartedExecutionEntitiesToUnknown(Date updateTime) {
        updateQueryGetResult(getEntityManager().createNativeQuery("select cast(UpdateStartedExecutionEntitiesToUnknown as text) from UpdateStartedExecutionEntitiesToUnknown(?)")
                .setParameter(1, updateTime));
    }

    @Override
    public void deleteRunningJobsOfTasklessCommands() {
        updateQueryGetResult(getEntityManager().createNativeQuery("select cast(DeleteRunningJobsOfTasklessCommands as text) from DeleteRunningJobsOfTasklessCommands()"));
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteCompletedJobs(final Date succeededJobs, final Date failedJobs) {
        updateQuery(getEntityManager().createNamedQuery("Job.deleteCompletedJobs")
                .setParameter("successEndTime", succeededJobs)
                .setParameter("failedEndTime", failedJobs)
                .setParameter("failStatus",
                        EnumSet.of(JobExecutionStatus.FAILED,
                                JobExecutionStatus.ABORTED,
                                JobExecutionStatus.UNKNOWN))
                .setParameter("successStatus", JobExecutionStatus.FINISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIfJobHasTasks(Guid jobId) {
        List<Step> steps = stepDao.getStepsByJobIdForVdsmAndGluster(jobId);

        return !steps.isEmpty();
    }
}
