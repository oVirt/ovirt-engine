package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.utils.crypt.TicketUtils;

public class SignStringQuery<P extends SignStringParameters> extends QueriesCommandBase<P> {
    public SignStringQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);

        try {
            TicketUtils ticketUtils = TicketUtils.getInstanceForEngineStoreSigning();

            String ticket = ticketUtils.generateTicket(getParameters().getString());
            getQueryReturnValue().setReturnValue(ticket);

            getQueryReturnValue().setSucceeded(true);
        } catch (Exception e) {
            log.error("Error when signing string: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

}
