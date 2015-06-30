package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.job.ExternalSystemType;
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
public class StepDaoImpl extends AbstractJpaDao<Step, Guid> implements StepDao {

    @Inject
    private JobDao jobDao;

    protected StepDaoImpl() {
        super(Step.class);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Guid id) {
        return get(id) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Step> getStepsByJobId(Guid jobId) {
        return jobDao.get(jobId).getSteps();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Step> getStepsByParentStepId(Guid parentStepId) {
        return get(parentStepId).getSteps();
    }

    @Override
    public void updateJobStepsCompleted(Guid jobId, JobExecutionStatus status, Date endTime) {
        if (status != JobExecutionStatus.STARTED) {
            updateQuery(getEntityManager().createNamedQuery("Step.updateJobStepsCompleted")
                    .setParameter("jobId", jobId)
                    .setParameter("status", status)
                    .setParameter("endTime", endTime)
                    .setParameter("startedStatus", JobExecutionStatus.STARTED));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Step> getStepsByExternalId(Guid externalId) {
        return multipleResults(getEntityManager().createNamedQuery("Step.getStepsByExternalId", Step.class)
                .setParameter("externalId", externalId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guid> getExternalIdsForRunningSteps(ExternalSystemType systemType) {
        return multipleResults(getEntityManager().createNamedQuery("Step.getExternalIdsForRunningSteps")
                        .setParameter("status", JobExecutionStatus.STARTED)
                        .setParameter("type", systemType)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Step> getStepsByJobIdForVdsmAndGluster(Guid jobId) {
        return multipleResults(getEntityManager().createNamedQuery("Step.getStepsByJobIdForVdsmAndGluster",
                Step.class)
                .setParameter("jobId", jobId)
                .setParameter("systemType", EnumSet.of(ExternalSystemType.VDSM, ExternalSystemType.GLUSTER)));
    }
}
