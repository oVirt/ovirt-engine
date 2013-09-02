package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;

public class HSMRevertTaskVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HSMRevertTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().revertTask(getParameters().getTaskId().toString());
        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());

        switch (returnStatus) {
        case UnknownTask:
            log.error(String.format("Trying to revert unknown task: %1$s", getParameters().getTaskId()));
            return;
        }
        super.proceedProxyReturnValue();
    }
}
