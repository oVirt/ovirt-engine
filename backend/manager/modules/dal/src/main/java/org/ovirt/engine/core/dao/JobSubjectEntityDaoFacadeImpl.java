package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.job.JobSubjectEntity;
import org.ovirt.engine.core.common.job.JobSubjectEntityId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;

/**
 * Implements the CRUD operations for job_subject_entity, a satellite table of Job.
 *
 */
@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class JobSubjectEntityDaoFacadeImpl extends AbstractJpaDao<JobSubjectEntity, JobSubjectEntityId> implements JobSubjectEntityDao {

    protected JobSubjectEntityDaoFacadeImpl() {
        super(JobSubjectEntity.class);
    }

    @Override
    public void save(Guid jobId, Guid entityId, VdcObjectType entityType) {
        super.save(new JobSubjectEntity(jobId, entityId, entityType));
    }

    @Override
    public Map<Guid, VdcObjectType> getJobSubjectEntityByJobId(Guid jobId) {
        List<JobSubjectEntity> list =
                multipleResults(entityManager.createNamedQuery("JobSubjectEntity.getJobSubjectEntityByJobId",
                        JobSubjectEntity.class)
                        .setParameter("jobId", jobId));

        Map<Guid, VdcObjectType> entityMap = new HashMap<Guid, VdcObjectType>();
        for (JobSubjectEntity jobSubjectEntity : list) {
            entityMap.put(jobSubjectEntity.getEntityId(), jobSubjectEntity.getEntityType());
        }
        return entityMap;
    }

    @Override
    public List<Guid> getJobIdByEntityId(Guid entityId) {
        return multipleResults(entityManager.createNamedQuery("JobSubjectEntity.getJobIdByEntityId")
                .setParameter("entityId", entityId));
    }
}
