package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SetVmTicketVDSCommand<P extends SetVmTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = new Guid();
    private String mTicket;
    private int mValidTime; // in seconds

    public SetVmTicketVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mTicket = parameters.getTicket();
        mValidTime = parameters.getValidTime();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().setVmTicket(mVmId.toString(), mTicket, (new Integer(mValidTime)).toString());
        ProceedProxyReturnValue();
    }
}
