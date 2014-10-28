package org.ovirt.engine.core.common.job;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class JobSubjectEntityId implements Serializable {
    private static final long serialVersionUID = 1740373688528083410L;
    private Guid entityId;
    private Guid jobId;

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
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
        if (!(obj instanceof JobSubjectEntityId)) {
            return false;
        }
        JobSubjectEntityId other = (JobSubjectEntityId) obj;
        return Objects.equals(entityId, other.entityId)
                && Objects.equals(jobId, other.jobId);
    }

}
