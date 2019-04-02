package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.NbdServerVDSParameters;

public class StopNbdServerVDSCommand<P extends NbdServerVDSParameters> extends VdsBrokerCommand<P> {

    public StopNbdServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().stopNbdServer(
                getParameters().getServerId().toString());
        proceedProxyReturnValue();
    }
}
