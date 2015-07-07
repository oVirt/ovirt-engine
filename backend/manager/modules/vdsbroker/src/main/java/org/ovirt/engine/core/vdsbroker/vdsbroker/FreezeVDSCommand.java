package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class FreezeVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {

    public FreezeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().freeze(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
