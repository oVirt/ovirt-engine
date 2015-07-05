package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;

public class HSMClearTaskVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HSMClearTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().clearTask(getParameters().getTaskId().toString());
        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());

        switch (returnStatus) {
        case UnknownTask:
            log.error("Trying to remove unknown task '{}'", getParameters().getTaskId());
            return;
        case TaskStateError:
            initializeVdsError(returnStatus);
            getVDSReturnValue().setSucceeded(false);
            return;
        }
        super.proceedProxyReturnValue();
    }
}
