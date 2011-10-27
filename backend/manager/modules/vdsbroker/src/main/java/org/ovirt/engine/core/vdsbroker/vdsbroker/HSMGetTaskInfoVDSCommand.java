package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class HSMGetTaskInfoVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters>
        extends HSMGetAllTasksInfoVDSCommand<P> {
    private TaskInfoReturnForXmlRpc _result;

    public HSMGetTaskInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getTaskInfo(getParameters().getTaskId().toString());
        ProceedProxyReturnValue();
        setReturnValue(ParseTaskInfo(_result.TaskInfo, getParameters().getTaskId()));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }
}
