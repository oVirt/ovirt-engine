package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class CommandEntity implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 5293055556971973650L;
    private long engineSessionSeqId;
    private Guid userId;
    private Guid commandId;
    private Guid parentCommandId;
    private Guid rootCommandId;
    private Guid jobId;
    private Guid stepId;
    private VdcActionType commandType;
    private VdcActionParametersBase commandParameters;
    private VdcReturnValueBase returnValue;
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

    public VdcActionParametersBase getCommandParameters() {
        return this.commandParameters;
    }

    public void setCommandParameters(VdcActionParametersBase value) {
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
                                                   Guid jobId,
                                                   Guid stepId,
                                                   VdcActionType actionType,
                                                   VdcActionParametersBase params,
                                                   CommandStatus status,
                                                   boolean callbackEnabled,
                                                   VdcReturnValueBase returnValue,
                                                   Map<String, Serializable> data) {
        CommandEntity entity = new CommandEntity();
        entity.setEngineSessionSeqId(engineSessionSeqId);
        entity.setUserId(userId);
        entity.setId(commandId);
        entity.setParentCommandId(parentCommandId);
        entity.setRootCommandId(rootCommandId);
        entity.setJobId(jobId);
        entity.setStepId(stepId);
        entity.setCommandType(actionType);
        entity.setCommandParameters(params);
        entity.setCommandStatus(status);
        entity.setCallbackEnabled(callbackEnabled);
        entity.setReturnValue(returnValue);
        entity.setData(data);
        return entity;
    }

    public VdcReturnValueBase getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(VdcReturnValueBase returnValue) {
        this.returnValue = returnValue;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
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
}
