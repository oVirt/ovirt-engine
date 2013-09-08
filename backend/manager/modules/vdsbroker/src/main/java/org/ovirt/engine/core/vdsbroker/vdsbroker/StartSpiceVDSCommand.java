package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.StartSpiceVDSCommandParameters;

public class StartSpiceVDSCommand<P extends StartSpiceVDSCommandParameters> extends VdsBrokerCommand<P> {
    private String _ip;
    private int _port;
    private String _ticket;

    public StartSpiceVDSCommand(P parameters) {
        super(parameters);
        _ip = parameters.getVdsIp();
        _port = parameters.getGuestPort();
        _ticket = parameters.getTicket();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().startSpice(_ip, _port, _ticket);
        proceedProxyReturnValue();
    }
}
