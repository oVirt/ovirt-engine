package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

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
        return Objects.hash(
                super.hashCode(),
                commandId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CommandAssociatedEntity)) {
            return false;
        }
        CommandAssociatedEntity other = (CommandAssociatedEntity) obj;
        return super.equals(obj)
                && Objects.equals(commandId, other.commandId);
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
