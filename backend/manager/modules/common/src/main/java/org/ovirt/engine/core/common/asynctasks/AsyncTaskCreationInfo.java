package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskCreationInfo implements Serializable {

    private static final long serialVersionUID = -4968721903478353741L;

    private Guid vdsmTaskId;
    private Guid storagePoolId;
    private AsyncTaskType taskType;

    /**
     * The id of the step which monitors the task execution
     */
    private Guid stepId;

    public AsyncTaskCreationInfo() {
        this(Guid.Empty, AsyncTaskType.unknown, Guid.Empty);
    }

    public AsyncTaskCreationInfo(Guid vdsmTaskID, AsyncTaskType taskType, Guid storagePoolID) {
        setVdsmTaskId(vdsmTaskID);
        setTaskType(taskType);
        setStoragePoolID(storagePoolID);
    }

    public Guid getVdsmTaskId() {
        return vdsmTaskId;
    }

    public void setVdsmTaskId(Guid vdsmTaskId) {
        this.vdsmTaskId = vdsmTaskId;
    }

    public AsyncTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AsyncTaskType taskType) {
        this.taskType = taskType;
    }

    public Guid getStoragePoolID() {
        return storagePoolId;
    }

    public void setStoragePoolID(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

}
