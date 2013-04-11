package org.ovirt.engine.core.common.businessentities;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

public class CommandEntity implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 5293055556971973650L;
    private Guid commandId;
    private Guid parentCommandId;
    private VdcActionType commandType;
    private Map<String, Object> data = new HashMap<String, Object>();
    private Date createdAt;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commandId == null) ? 0 : commandId.hashCode());
        result = prime * result + ((commandType == null) ? 0 : commandType.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((parentCommandId == null) ? 0 : parentCommandId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CommandEntity))
            return false;
        CommandEntity other = (CommandEntity) obj;
        if (commandId == null) {
            if (other.commandId != null)
                return false;
        } else if (!commandId.equals(other.commandId))
            return false;
        if (commandType != other.commandType)
            return false;
        if (createdAt == null) {
            if (other.createdAt != null)
                return false;
        } else if (!createdAt.equals(other.createdAt))
            return false;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (parentCommandId == null) {
            if (other.parentCommandId != null)
                return false;
        } else if (!parentCommandId.equals(other.parentCommandId))
            return false;
        return true;
    }

    public Set<Guid> getChildCommandIds() {
        return childCommandIds;
    }

    public void setChildCommandIds(Set<Guid> childCommandIds) {
        this.childCommandIds = childCommandIds;
    }

    private Set<Guid> childCommandIds = Collections.newSetFromMap(new ConcurrentHashMap<Guid, Boolean>());

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Guid getId() {
        return commandId;
    }

    public void setId(Guid commandId) {
        this.commandId = commandId;
    }

    public Guid getParentCommandId() {
        return parentCommandId;
    }

    public void setParentCommandId(Guid parentCommandId) {
        this.parentCommandId = parentCommandId;
    }

    public VdcActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(VdcActionType type) {
        this.commandType = type;
    }
}
