package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.utils.PersistedCommandContext;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class CommandEntity implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 5293055556971973650L;
    private long engineSessionSeqId;
    private Guid userId;
    private Guid commandId;
    private Guid parentCommandId;
    private Guid rootCommandId;
    private PersistedCommandContext commandContext;
    private ActionType commandType;
    private ActionParametersBase commandParameters;
    private ActionReturnValue returnValue;
    private Date createdAt;
    private CommandStatus commandStatus;
    private boolean callbackEnabled;
    private boolean callbackNotified;
    private boolean executed;
    private boolean waitingForEvent;
    private Map<String, Serializable> data;

    public CommandEntity() {
        commandStatus = CommandStatus.UNKNOWN;
        createdAt = new Date();
        data = new HashMap<>();
        commandContext = new PersistedCommandContext();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                commandId,
                commandType,
                parentCommandId,
                rootCommandId,
                data
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CommandEntity)) {
            return false;
        }
        CommandEntity other = (CommandEntity) obj;
        return Objects.equals(commandId, other.commandId)
                && commandType == other.commandType
                && Objects.equals(getGuid(parentCommandId), getGuid(other.parentCommandId))
                && Objects.equals(getGuid(rootCommandId), getGuid(other.rootCommandId))
                && Objects.equals(data, other.data);
    }

    private Guid getGuid(Guid guid) {
        return Guid.isNullOrEmpty(guid) ? Guid.Empty : guid;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ActionParametersBase getCommandParameters() {
        return this.commandParameters;
    }

    public void setCommandParameters(ActionParametersBase value) {
        this.commandParameters = value;
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

    public Guid getRootCommandId() {
        return rootCommandId;
    }

    public void setRootCommandId(Guid rootCommandId) {
        this.rootCommandId = rootCommandId;
    }

    public ActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(ActionType type) {
        this.commandType = type;
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }

    public boolean isCallbackEnabled() {
        return callbackEnabled;
    }

    public void setCallbackEnabled(boolean callbackEnabled) {
        this.callbackEnabled = callbackEnabled;
    }

    public boolean isCallbackNotified() {
        return callbackNotified;
    }

    public void setCallbackNotified(boolean callbackNotified) {
        this.callbackNotified = callbackNotified;
    }

    public void setData(Map<String, Serializable> data) {
        this.data = data;
    }

    public Map<String, Serializable> getData() {
        return data;
    }

    public static CommandEntity buildCommandEntity(Guid userId,
                                                   long engineSessionSeqId,
                                                   Guid commandId,
                                                   Guid parentCommandId,
                                                   Guid rootCommandId,
                                                   PersistedCommandContext commandContext,
                                                   ActionType actionType,
                                                   ActionParametersBase params,
                                                   CommandStatus status,
                                                   boolean callbackEnabled,
                                                   ActionReturnValue returnValue,
                                                   Map<String, Serializable> data) {
        CommandEntity entity = new CommandEntity();
        entity.setEngineSessionSeqId(engineSessionSeqId);
        entity.setUserId(userId);
        entity.setId(commandId);
        entity.setParentCommandId(parentCommandId);
        entity.setRootCommandId(rootCommandId);
        if (commandContext != null) {
            entity.setCommandContext(commandContext);
        }
        entity.setCommandType(actionType);
        entity.setCommandParameters(params);
        entity.setCommandStatus(status);
        entity.setCallbackEnabled(callbackEnabled);
        entity.setReturnValue(returnValue);
        entity.setData(data);
        return entity;
    }

    public ActionReturnValue getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(ActionReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public long getEngineSessionSeqId() {
        return engineSessionSeqId;
    }

    public void setEngineSessionSeqId(long engineSessionSeqId) {
        this.engineSessionSeqId = engineSessionSeqId;
    }

    public boolean isWaitingForEvent() {
        return waitingForEvent;
    }

    public void setWaitingForEvent(boolean waitingForEvent) {
        this.waitingForEvent = waitingForEvent;
    }

    public PersistedCommandContext getCommandContext() {
        return commandContext;
    }

    public void setCommandContext(PersistedCommandContext commandContext) {
        this.commandContext = commandContext;
    }
}
