package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;

public class HSMRevertTaskVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HSMRevertTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().revertTask(getParameters().getTaskId().toString());
        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());

        switch (returnStatus) {
        case UnknownTask:
            log.error("Trying to revert unknown task '{}'", getParameters().getTaskId());
            return;
        }
        super.proceedProxyReturnValue();
    }
}
