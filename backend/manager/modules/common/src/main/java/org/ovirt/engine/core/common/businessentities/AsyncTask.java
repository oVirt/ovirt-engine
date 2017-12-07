package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class AsyncTask implements Serializable {
    private static final long serialVersionUID = 5913365704117183118L;
    private AsyncTaskResultEnum result;
    private AsyncTaskStatusEnum status;
    private Guid userId;
    private Guid vdsmTaskId;
    private Guid storagePoolId;
    private Guid taskId;
    private Guid commandId;
    private Guid rootCommandId;
    private Guid stepId;
    private AsyncTaskType taskType;
    private Date startTime;
    private CommandEntity rootCmdEntity;
    private CommandEntity childCmdEntity;

    public AsyncTask() {
        result = AsyncTaskResultEnum.success;
        status = AsyncTaskStatusEnum.unknown;
        userId = Guid.Empty;
        vdsmTaskId = Guid.Empty;
        commandId = Guid.Empty;
        rootCommandId = Guid.Empty;
        rootCmdEntity = new CommandEntity();
        childCmdEntity = new CommandEntity();
    }

    public AsyncTask(AsyncTaskResultEnum result,
                     AsyncTaskStatusEnum status,
                     Guid userId,
                     Guid vdsmTaskId,
                     Guid stepId,
                     Guid storagePoolId,
                     AsyncTaskType taskType,
                     CommandEntity rootCmdEntity,
                     CommandEntity childCmdEntity) {
        this.result = result;
        this.status = status;
        this.userId = userId;
        this.vdsmTaskId = vdsmTaskId;
        this.stepId = stepId;
        this.startTime = new Date();
        this.commandId = childCmdEntity.getId();
        this.rootCommandId = rootCmdEntity.getId();
        this.storagePoolId = storagePoolId;
        this.taskId = Guid.newGuid();
        this.taskType = taskType;
        this.rootCmdEntity = rootCmdEntity;
        this.childCmdEntity = childCmdEntity;
    }

    public ActionType getActionType() {
        return rootCmdEntity.getCommandType();
    }

    public void setActionType(ActionType value) {
        this.rootCmdEntity.setCommandType(value);
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

    public ActionParametersBase getActionParameters() {
        return rootCmdEntity.getCommandParameters();
    }

    public void setActionParameters(ActionParametersBase value) {
        this.rootCmdEntity.setCommandParameters(value);
    }

    public ActionParametersBase getTaskParameters() {
        return childCmdEntity.getCommandParameters();
    }

    public void setTaskParameters(ActionParametersBase value) {
        childCmdEntity.setCommandParameters(value);
    }

    public Guid getStepId() {
        return this.stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    public CommandStatus getCommandStatus() {
        return childCmdEntity.getCommandStatus();
    }

    public void setCommandStatus(CommandStatus status) {
        childCmdEntity.setCommandStatus(status);
    }

    public void setCommandType(ActionType cmdType) {
        childCmdEntity.setCommandType(cmdType);
    }

    public ActionType getCommandType() {
        return childCmdEntity.getCommandType();
    }

    public void setCreatedAt(Date createdAt) {
        childCmdEntity.setCreatedAt(createdAt);
    }

    public Date getCreatedAt() {
        return childCmdEntity.getCreatedAt();
    }

    public Guid getRootCommandId() {
        return rootCommandId;
    }

    public void setRootCommandId(Guid rootCommandId) {
        this.rootCommandId = rootCommandId;
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
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

    public void setCallbackEnabled(boolean enabled) {
        childCmdEntity.setCallbackEnabled(enabled);
    }

    public boolean isCallbackEnabled() {
        return childCmdEntity.isCallbackEnabled();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                vdsmTaskId,
                stepId,
                commandId,
                rootCmdEntity,
                result,
                status,
                startTime,
                storagePoolId,
                taskType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AsyncTask)) {
            return false;
        }
        AsyncTask other = (AsyncTask) obj;
        return Objects.equals(vdsmTaskId, other.vdsmTaskId)
                && Objects.equals(taskId, other.taskId)
                && Objects.equals(stepId, other.stepId)
                && Objects.equals(commandId, other.commandId)
                && Objects.equals(rootCommandId, other.rootCommandId)
                && result == other.result
                && status == other.status
                && Objects.equals(startTime, other.startTime)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(taskType, other.taskType);
    }

    public CommandEntity getRootCmdEntity() {
        return rootCmdEntity;
    }

    public void setRootCmdEntity(CommandEntity rootCmdEntity) {
        this.rootCmdEntity = rootCmdEntity;
    }

    public CommandEntity getChildCmdEntity() {
        return childCmdEntity;
    }

    public void setChildCmdEntity(CommandEntity childCmdEntity) {
        this.childCmdEntity = childCmdEntity;
    }

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        ToStringBuilder stringBuilder = ToStringBuilder.forInstance(this);
        stringBuilder.append("commandId", commandId)
                .append("rootCommandId", rootCommandId)
                .append("storagePoolId", storagePoolId)
                .append("taskId", taskId)
                .append("vdsmTaskId", vdsmTaskId)
                .append("stepId", stepId)
                .append("taskType", taskType)
                .append("status", status);

        return stringBuilder.build();
    }
}
