package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("ticket", getTicket())
                .append("guestPort", getGuestPort())
                .append("vdsIp", getVdsIp());
    }
}
