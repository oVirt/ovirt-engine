package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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

    public VdcActionType getActionType() {
        return rootCmdEntity.getCommandType();
    }

    public void setActionType(VdcActionType value) {
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

    public VdcActionParametersBase getActionParameters() {
        return rootCmdEntity.getCommandParameters();
    }

    public void setActionParameters(VdcActionParametersBase value) {
        this.rootCmdEntity.setCommandParameters(value);
    }

    public VdcActionParametersBase getTaskParameters() {
        return childCmdEntity.getCommandParameters();
    }

    public void setTaskParameters(VdcActionParametersBase value) {
        childCmdEntity.setCommandParameters(value);
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
        return getActionType();
    }

    public CommandStatus getCommandStatus() {
        return childCmdEntity.getCommandStatus();
    }

    public void setCommandStatus(CommandStatus status) {
        childCmdEntity.setCommandStatus(status);
    }

    public void setCommandType(VdcActionType cmdType) {
        childCmdEntity.setCommandType(cmdType);
    }

    public VdcActionType getCommandType() {
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

    public void setCallBackEnabled(boolean enabled) {
        childCmdEntity.setCallbackEnabled(enabled);
    }

    public boolean isCallBackEnabled() {
        return childCmdEntity.isCallbackEnabled();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int results = 1;
        results = prime * results + ((vdsmTaskId == null) ? 0 : vdsmTaskId.hashCode());
        results = prime * results + ((stepId == null) ? 0 : stepId.hashCode());
        results = prime * results + ((commandId == null) ? 0 : commandId.hashCode());
        results = prime * results + ((rootCmdEntity.getCommandType() == null) ? 0 : rootCmdEntity.getCommandType().hashCode());
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
        AsyncTask other = (AsyncTask) obj;
        return (ObjectUtils.objectsEqual(vdsmTaskId, other.vdsmTaskId)
                && ObjectUtils.objectsEqual(taskId, other.taskId)
                && ObjectUtils.objectsEqual(stepId, other.stepId)
                && ObjectUtils.objectsEqual(commandId, other.commandId)
                && ObjectUtils.objectsEqual(rootCommandId, other.rootCommandId)
                && result == other.result
                && status == other.status
                && ObjectUtils.objectsEqual(startTime, other.startTime)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && ObjectUtils.objectsEqual(taskType, other.taskType));
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
}
