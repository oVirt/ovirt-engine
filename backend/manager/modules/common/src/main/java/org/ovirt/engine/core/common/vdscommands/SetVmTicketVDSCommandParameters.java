package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SetVmTicketVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String ticket;
    private int validTime;
    private String userName;
    private Guid userId;
    private GraphicsType graphicsType;

    public SetVmTicketVDSCommandParameters() {
    }

    public SetVmTicketVDSCommandParameters(Guid vdsId, Guid vmId, String ticket, int validTime, String userName,
                                           Guid userId, GraphicsType graphicsType)
    {
        super(vdsId, vmId);
        this.ticket = ticket;
        this.validTime = validTime;
        this.userName = userName;
        this.userId = userId;
        this.graphicsType = graphicsType;
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

    public GraphicsType getGraphicsType() {
        return graphicsType;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("protocol", graphicsType)
                .append("ticket", getTicket())
                .append("validTime", getValidTime())
                .append("userName", getUserName())
                .append("userId", getUserId());
    }
}
