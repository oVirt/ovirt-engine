package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SetVmTicketVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String ticket;
    private int validTime;
    private String userName;
    private Guid userId;

    public SetVmTicketVDSCommandParameters(Guid vdsId, Guid vmId, String ticket, int validTime, String userName, Guid userId) {
        super(vdsId, vmId);
        this.ticket = ticket;
        this.validTime = validTime;
        this.userName = userName;
        this.userId = userId;
    }

    public String getTicket() {
        return ticket;
    }

    public int getValidTime() {
        return validTime;
    }

    public Guid getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public SetVmTicketVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, ticket=%s, validTime=%s,m userName=%s, userId=%s", super.toString(), getTicket(), getValidTime(),
                getUserName(), getUserId());
    }
}
