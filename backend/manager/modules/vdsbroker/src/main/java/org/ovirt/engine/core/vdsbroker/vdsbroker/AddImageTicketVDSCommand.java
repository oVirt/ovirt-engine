package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.AddImageTicketVDSCommandParameters;

public class AddImageTicketVDSCommand<P extends AddImageTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    StatusOnlyReturn retval;

    public AddImageTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        retval = getBroker().add_image_ticket(getParameters().getImageTicket());
        proceedProxyReturnValue();
        setReturnValue(retval);
    }


    @Override
    protected Status getReturnStatus() {
        return retval.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return retval;
    }
}
