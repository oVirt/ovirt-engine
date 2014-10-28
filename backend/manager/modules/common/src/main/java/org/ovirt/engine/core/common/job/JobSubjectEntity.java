package org.ovirt.engine.core.common.job;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "job_subject_entity")
@Cacheable(true)
@IdClass(JobSubjectEntityId.class)
@NamedQueries({
        @NamedQuery(
                name = "JobSubjectEntity.getJobSubjectEntityByJobId",
                query = "select j from JobSubjectEntity j where j.jobId = :jobId"),
        @NamedQuery(
                name = "JobSubjectEntity.getJobIdByEntityId",
                query = "select j.jobId from JobSubjectEntity j where j.entityId = :entityId")
})
public class JobSubjectEntity implements BusinessEntity<JobSubjectEntityId> {
    private static final long serialVersionUID = 2914574412875016659L;

    @Id
    @Column(name = "entity_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid entityId;

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private VdcObjectType entityType;

    @Id
    @Column(name = "job_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid jobId;

    public JobSubjectEntity() {
    }

    public JobSubjectEntity(Guid jobId, Guid entityId, VdcObjectType entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.jobId = jobId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityType(VdcObjectType entityType) {
        this.entityType = entityType;
    }

    public VdcObjectType getEntityType() {
        return entityType;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    @Override
    public JobSubjectEntityId getId() {
        JobSubjectEntityId key = new JobSubjectEntityId();
        key.setEntityId(entityId);
        key.setJobId(jobId);
        return key;
    }

    @Override
    public void setId(JobSubjectEntityId id) {
        entityId = id.getEntityId();
        jobId = id.getJobId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, jobId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JobSubjectEntity)) {
            return false;
        }
        JobSubjectEntity other = (JobSubjectEntity) obj;
        return Objects.equals(entityId, other.entityId) &&
                Objects.equals(jobId, other.jobId);
    }
}
