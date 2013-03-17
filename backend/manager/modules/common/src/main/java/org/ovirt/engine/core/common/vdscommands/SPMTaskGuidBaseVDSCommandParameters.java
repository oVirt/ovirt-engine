package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SPMTaskGuidBaseVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public SPMTaskGuidBaseVDSCommandParameters(Guid storagePoolId, Guid taskId) {
        super(storagePoolId);
        setTaskId(taskId);
    }

    private Guid privateTaskId = new Guid();

    public Guid getTaskId() {
        return privateTaskId;
    }

    private void setTaskId(Guid value) {
        privateTaskId = value;
    }

    public SPMTaskGuidBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, taskId = %s", super.toString(), getTaskId());
    }
}
