package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskEntity {

    private Guid taskId;
    private VdcObjectType entityType;
    private Guid entityId;

    public AsyncTaskEntity() {
    }

    public AsyncTaskEntity(Guid taskId, VdcObjectType entityType, Guid entityId) {
        this.taskId = taskId;
        this.entityType = entityType;
        this.entityId = entityId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result
                + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AsyncTaskEntity other = (AsyncTaskEntity) obj;
        return Objects.equals(taskId, other.taskId) &&
                Objects.equals(entityId, other.entityId) &&
                Objects.equals(entityType, other.entityType);
    }


    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getTaskId() {
        return taskId;
    }

    public void setTaskId(Guid taskId) {
        this.taskId = taskId;
    }

    public VdcObjectType getEntityType() {
        return entityType;
    }

    public void setEntityType(VdcObjectType entityType) {
        this.entityType = entityType;
    }

}
