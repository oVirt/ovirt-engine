package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.TRACE)
public class HSMGetTaskStatusVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters>
        extends HSMGetAllTasksStatusesVDSCommand<P> {
    private TaskStatusReturnForXmlRpc _result;

    public HSMGetTaskStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getTaskStatus(getParameters().getTaskId().toString());
        ProceedProxyReturnValue();
        setReturnValue(ParseTaskStatus(_result.TaskStatus));
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case UnknownTask:
            // ignore this, the parser can handle the empty result.
            break;

        default:
            super.ProceedProxyReturnValue();
            InitializeVdsError(returnStatus);
            break;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected void UpdateReturnStatus(StatusForXmlRpc newReturnStatus) {
        _result.mStatus = newReturnStatus;
    }
}
