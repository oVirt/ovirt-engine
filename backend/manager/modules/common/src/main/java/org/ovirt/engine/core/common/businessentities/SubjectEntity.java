package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SubjectEntity {
    private Guid entityId;
    private VdcObjectType entityType;

    public SubjectEntity() {
    }

    public SubjectEntity(VdcObjectType entityType, Guid entityId) {
        setEntityType(entityType);
        setEntityId(entityId);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(entityId);
        result = prime * result + Objects.hashCode(entityType);
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
        if (!(obj instanceof SubjectEntity)) {
            return false;
        }
        SubjectEntity other = (SubjectEntity) obj;
        return Objects.equals(entityId, other.entityId) &&
                Objects.equals(entityType, other.entityType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("entityId", entityId)
                .append("entityType", entityType)
                .build();
    }
}
