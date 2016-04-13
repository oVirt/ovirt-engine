package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.AddImageTicketVDSCommandParameters;

public class AddImageTicketVDSCommand<P extends AddImageTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    StatusOnlyReturnForXmlRpc retval;

    public AddImageTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        retval = getBroker().add_image_ticket(
                        getParameters().getTicketId().toString(),
                        getParameters().getOperations(),
                        getParameters().getTimeout(),
                        getParameters().getSize(),
                        getParameters().getUrl());

        proceedProxyReturnValue();
        setReturnValue(retval);
    }


    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return retval.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return retval;
    }
}
