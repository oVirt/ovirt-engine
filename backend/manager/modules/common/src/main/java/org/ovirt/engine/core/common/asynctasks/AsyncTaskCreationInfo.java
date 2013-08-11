package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskCreationInfo implements Serializable {

    private Guid vdsmTaskID;
    private Guid privateStoragePoolID;
    private AsyncTaskType privateTaskType;

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
        return vdsmTaskID;
    }

    public void setVdsmTaskId(Guid value) {
        vdsmTaskID = value;
    }

    public AsyncTaskType getTaskType() {
        return privateTaskType;
    }

    public void setTaskType(AsyncTaskType value) {
        privateTaskType = value;
    }

    public Guid getStoragePoolID() {
        return privateStoragePoolID;
    }

    public void setStoragePoolID(Guid value) {
        privateStoragePoolID = value;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

}
