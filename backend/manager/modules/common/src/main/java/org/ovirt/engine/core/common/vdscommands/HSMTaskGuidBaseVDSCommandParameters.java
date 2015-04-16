package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HSMTaskGuidBaseVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public HSMTaskGuidBaseVDSCommandParameters(Guid vdsId, Guid taskId) {
        super(vdsId);
        setTaskId(taskId);
    }

    private Guid privateTaskId;

    public Guid getTaskId() {
        return privateTaskId;
    }

    private void setTaskId(Guid value) {
        privateTaskId = value;
    }

    public HSMTaskGuidBaseVDSCommandParameters() {
        privateTaskId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("taskId", getTaskId());
    }
}
