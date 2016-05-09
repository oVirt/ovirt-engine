package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ExtendImageTicketVDSCommandParameters;

public class ExtendImageTicketVDSCommand <P extends ExtendImageTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    public ExtendImageTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().extend_image_ticket(
                getParameters().getTicketId().toString(),
                getParameters().getTimeout());

        proceedProxyReturnValue();
        setReturnValue(status);
    }
}
