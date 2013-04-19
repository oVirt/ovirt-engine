package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskCreationInfo implements Serializable {

    private Guid vdsmTaskID = Guid.Empty;
    private Guid privateStoragePoolID = Guid.Empty;
    private AsyncTaskType privateTaskType = AsyncTaskType.forValue(0);

    /**
     * The id of the step which monitors the task execution
     */
    private Guid stepId = null;

    public AsyncTaskCreationInfo() {
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
