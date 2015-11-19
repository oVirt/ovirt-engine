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
        return Objects.hash(
                entityId,
                entityType,
                taskId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AsyncTaskEntity)) {
            return false;
        }
        AsyncTaskEntity other = (AsyncTaskEntity) obj;
        return Objects.equals(taskId, other.taskId)
                && Objects.equals(entityId, other.entityId)
                && Objects.equals(entityType, other.entityType);
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
