package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;

public class DestroyVDSCommand<P extends DestroyVmVDSCommandParameters> extends VdsBrokerCommand<P> {
    public DestroyVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (getParameters().getGracefully()) {
            status = getBroker().shutdown(getParameters().getVmId().toString(),
                    String.valueOf(getParameters().getSecondsToWait()),
                    Config.<String> getValue(ConfigValues.VmGracefulShutdownMessage));
        } else {
            status = getBroker().destroy(getParameters().getVmId().toString());
        }
        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
            EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
            switch (returnStatus) {
                case noVM:
                    if (getParameters().isIgnoreNoVm()) {// do not log error
                        log.info("Failed to destroy VM '{}' because VM does not exist, ignoring", getParameters().getVmId());
                        return;
                    }

                    log.info("Destroy VM couldn't find VM '{}'. If after Cancel Migration and VM is UP on source, " +
                            "ignore next error, it's just a clean-up call", getParameters().getVmId());
                    // do not break here
                default:
                    super.proceedProxyReturnValue();
            }
    }
}
