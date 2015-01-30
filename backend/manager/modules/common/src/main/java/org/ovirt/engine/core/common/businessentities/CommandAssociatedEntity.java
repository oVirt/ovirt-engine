package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

import java.util.Objects;

public class CommandAssociatedEntity extends SubjectEntity {

    private Guid commandId;

    public CommandAssociatedEntity() {
    }

    public CommandAssociatedEntity(Guid commandId, VdcObjectType entityType, Guid entityId) {
        super(entityType, entityId);
        this.commandId = commandId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(commandId);
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
        CommandAssociatedEntity other = (CommandAssociatedEntity) obj;
        return Objects.equals(commandId, other.commandId) &&
                super.equals(obj);
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public Guid getCommandId() {
        return commandId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("commandId", commandId)
                .append("entityId", getEntityId())
                .append("entityType", getEntityType())
                .build();
    }
}
