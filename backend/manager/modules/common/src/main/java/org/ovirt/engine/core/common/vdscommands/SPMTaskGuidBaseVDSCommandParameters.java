package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SPMTaskGuidBaseVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public SPMTaskGuidBaseVDSCommandParameters(Guid storagePoolId, Guid taskId) {
        super(storagePoolId);
        setTaskId(taskId);
    }

    private Guid privateTaskId;

    public Guid getTaskId() {
        return privateTaskId;
    }

    private void setTaskId(Guid value) {
        privateTaskId = value;
    }

    public SPMTaskGuidBaseVDSCommandParameters() {
        privateTaskId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("taskId", getTaskId());
    }
}
