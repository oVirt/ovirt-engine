package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SetVmTicketParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 7066467049851637162L;

    private String _ticket;

    private int _validTime;

    private String _clientIp;

    public SetVmTicketParameters(Guid vmId, String ticket, int validTime) {
        super(vmId);
        _ticket = ticket;
        _validTime = validTime;
    }

    public SetVmTicketParameters(Guid vmId, String ticket, int validTime, String clientIp) {
        this(vmId, ticket, validTime);
        _clientIp = clientIp;
        _ticket = ticket;
        _validTime = validTime;
    }

    public String getTicket() {
        return _ticket;
    }

    public int getValidTime() {
        return _validTime;
    }

    public String getClientIp() {
        return _clientIp;
    }

    public SetVmTicketParameters() {
    }
}
