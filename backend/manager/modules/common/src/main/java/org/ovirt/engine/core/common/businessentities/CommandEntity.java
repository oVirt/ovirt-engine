package org.ovirt.engine.core.common.businessentities;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class CommandEntity implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 5293055556971973650L;
    private Guid commandId;
    private Guid rootCommandId;
    private VdcActionType commandType;
    private VdcActionParametersBase actionParameters;
    private Date createdAt;
    private CommandStatus commandStatus = CommandStatus.UNKNOWN;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commandId == null) ? 0 : commandId.hashCode());
        result = prime * result + ((commandType == null) ? 0 : commandType.hashCode());
        result = prime * result + ((rootCommandId == null) ? 0 : rootCommandId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommandEntity other = (CommandEntity) obj;
        return ObjectUtils.objectsEqual(commandId, other.commandId)
                && commandType == other.commandType
                && ObjectUtils.objectsEqual(rootCommandId, other.rootCommandId);
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

    public VdcActionParametersBase getActionParameters() {
        return this.actionParameters;
    }

    public void setActionParameters(VdcActionParametersBase value) {
        this.actionParameters = value;
    }

    public Guid getId() {
        return commandId;
    }

    public void setId(Guid commandId) {
        this.commandId = commandId;
    }

    public Guid getRootCommandId() {
        return rootCommandId;
    }

    public void setRootCommandId(Guid rootCommandId) {
        this.rootCommandId = rootCommandId;
    }

    public VdcActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(VdcActionType type) {
        this.commandType = type;
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }
}
