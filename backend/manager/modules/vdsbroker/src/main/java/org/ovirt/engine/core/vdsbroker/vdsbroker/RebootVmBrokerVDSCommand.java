package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class RebootVmBrokerVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {

    public RebootVmBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        final Integer timeout = Config.getValue(ConfigValues.VmGracefulShutdownTimeout);
        final String message = Config.getValue(ConfigValues.VmGracefulShutdownMessage);
        status = getBroker().shutdown(getParameters().getVmId().toString(), timeout.toString(), message, true);
        proceedProxyReturnValue();
    }
}
