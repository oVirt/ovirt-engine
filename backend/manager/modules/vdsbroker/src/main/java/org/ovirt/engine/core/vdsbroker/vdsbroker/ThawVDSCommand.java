package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class ThawVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {

    public ThawVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().thaw(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
