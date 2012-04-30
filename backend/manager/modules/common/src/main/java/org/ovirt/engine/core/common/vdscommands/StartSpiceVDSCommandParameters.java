package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class StartSpiceVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private String _vdsIp;
    private String _ticket;
    private int _guestPort;

    public StartSpiceVDSCommandParameters(Guid vdsId, String vdsIp, int guestPort, String ticket) {
        super(vdsId);
        _vdsIp = vdsIp;
        _ticket = ticket;
        _guestPort = guestPort;
    }

    public String getTicket() {
        return _ticket;
    }

    public int getGuestPort() {
        return _guestPort;
    }

    public String getVdsIp() {
        return _vdsIp;
    }

    public StartSpiceVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, ticket=%s, guestPort=%s, vdsIp=%s",
                super.toString(),
                getTicket(),
                getGuestPort(),
                getVdsIp());
    }
}
