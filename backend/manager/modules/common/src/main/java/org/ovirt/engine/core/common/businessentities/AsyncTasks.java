package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class AsyncTasks implements Serializable {
    private static final long serialVersionUID = 5913365704117183118L;
    private CommandEntity cmdEntity;
    private VdcActionType actionType;
    private AsyncTaskResultEnum result;
    private Date startTime;
    private Guid vdsmTaskId;
    private Guid storagePoolId;
    private VdcActionParametersBase actionParameters;
    private Guid taskId;
    private Guid commandId;
    private Guid stepId;
    private AsyncTaskType taskType;

    public AsyncTasks() {
        actionType = VdcActionType.Unknown;
        result = AsyncTaskResultEnum.success;
        status = AsyncTaskStatusEnum.unknown;
        vdsmTaskId = Guid.Empty;
        commandId = Guid.Empty;
        cmdEntity = new CommandEntity();
        cmdEntity.setId(commandId);
        cmdEntity.setRootCommandId(Guid.Empty);
        cmdEntity.setCommandType(VdcActionType.Unknown);
    }

    public AsyncTasks(VdcActionType action_type,
                      AsyncTaskResultEnum result,
                      AsyncTaskStatusEnum status,
                      Guid vdsmTaskId,
                      VdcActionParametersBase parentParameters,
                      VdcActionParametersBase taskParameters,
                      Guid stepId,
                      Guid commandId,
                      Guid rootCommandId,
                      Guid storagePoolId,
                      AsyncTaskType taskType,
                      CommandStatus cmdStatus) {
        this.actionType = action_type;
        this.result = result;
        this.status = status;
        this.vdsmTaskId = vdsmTaskId;
        this.actionParameters = parentParameters;
        this.stepId = stepId;
        this.startTime = new Date();
        this.commandId = commandId;
        this.storagePoolId = storagePoolId;
        this.taskId = Guid.newGuid();
        this.taskType = taskType;
        cmdEntity = new CommandEntity();
        cmdEntity.setId(commandId);
        cmdEntity.setRootCommandId(rootCommandId);
        cmdEntity.setCommandStatus(cmdStatus);
        cmdEntity.setCommandParameters(taskParameters);
        cmdEntity.setCommandType(taskParameters.getCommandType());
    }

    public VdcActionType getaction_type() {
        return actionType;
    }

    public void setaction_type(VdcActionType value) {
        this.actionType = value;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public AsyncTaskResultEnum getresult() {
        return this.result;
    }

    public void setresult(AsyncTaskResultEnum value) {
        this.result = value;
    }

    private AsyncTaskStatusEnum status;

    public AsyncTaskStatusEnum getstatus() {
        return this.status;
    }

    public void setstatus(AsyncTaskStatusEnum value) {
        this.status = value;
    }

    public Guid getVdsmTaskId() {
        return this.vdsmTaskId;
    }

    public void setVdsmTaskId(Guid value) {
        this.vdsmTaskId = value;
    }

    public Guid getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Guid value) {
        this.taskId = value;
    }

    public VdcActionParametersBase getActionParameters() {
        return actionParameters;
    }

    public void setActionParameters(VdcActionParametersBase value) {
        this.actionParameters = value;
    }

    public VdcActionParametersBase getTaskParameters() {
        return cmdEntity.getCommandParameters();
    }

    public void setTaskParameters(VdcActionParametersBase value) {
        cmdEntity.setCommandParameters(value);
    }

    public Guid getStepId() {
        return this.stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    private VdcActionType getEndActionType() {
        VdcActionType commandType = getTaskParameters().getCommandType();
        if (!VdcActionType.Unknown.equals(commandType)) {
            return commandType;
        }
        return getaction_type();
    }

    public CommandStatus getCommandStatus() {
        return cmdEntity.getCommandStatus();
    }

    public void setCommandStatus(CommandStatus status) {
        cmdEntity.setCommandStatus(status);
    }

    public void setCommandType(VdcActionType cmdType) {
        cmdEntity.setCommandType(cmdType);
    }

    public VdcActionType getCommandType() {
        return cmdEntity.getCommandType();
    }

    public void setCreatedAt(Date createdAt) {
        cmdEntity.setCreatedAt(createdAt);
    }

    public Date getCreatedAt() {
        return cmdEntity.getCreatedAt();
    }

    public Guid getRootCommandId() {
        return cmdEntity.getRootCommandId();
    }

    public void setRootCommandId(Guid rootCommandId) {
        cmdEntity.setRootCommandId(rootCommandId);
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
        this.cmdEntity.setId(commandId);
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public AsyncTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AsyncTaskType taskType) {
        this.taskType = taskType;
    }

    public void setCallBackEnabled(boolean enabled) {
        cmdEntity.setCallBackEnabled(enabled);
    }

    public boolean isCallBackEnabled() {
        return cmdEntity.isCallBackEnabled();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int results = 1;
        results = prime * results + ((vdsmTaskId == null) ? 0 : vdsmTaskId.hashCode());
        results = prime * results + ((stepId == null) ? 0 : stepId.hashCode());
        results = prime * results + ((commandId == null) ? 0 : commandId.hashCode());
        results = prime * results + ((actionType == null) ? 0 : actionType.hashCode());
        results = prime * results + ((result == null) ? 0 : result.hashCode());
        results = prime * results + ((status == null) ? 0 : status.hashCode());
        results = prime * results + ((startTime == null) ? 0 : startTime.hashCode());
        results = prime * results + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        results = prime * results + ((taskType == null) ? 0 : taskType.hashCode());
        return results;
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
        AsyncTasks other = (AsyncTasks) obj;
        return (ObjectUtils.objectsEqual(vdsmTaskId, other.vdsmTaskId)
                && ObjectUtils.objectsEqual(taskId, other.taskId)
                && ObjectUtils.objectsEqual(stepId, other.stepId)
                && ObjectUtils.objectsEqual(commandId, other.commandId)
                && actionType == other.actionType
                && result == other.result
                && status == other.status
                && ObjectUtils.objectsEqual(startTime, other.startTime)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && ObjectUtils.objectsEqual(taskType, other.taskType));
    }
}
