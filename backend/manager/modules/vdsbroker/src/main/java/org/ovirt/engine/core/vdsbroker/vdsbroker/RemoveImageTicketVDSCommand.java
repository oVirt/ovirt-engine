package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.RemoveImageTicketVDSCommandParameters;

public class RemoveImageTicketVDSCommand<P extends RemoveImageTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    public RemoveImageTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().remove_image_ticket(getParameters().getTicketId().toString());
        proceedProxyReturnValue();
        setReturnValue(status);
    }
}
