package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class ResetVmBrokerVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {

    public ResetVmBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().reset(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
