package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
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
    public String toString() {
        return String.format("%s, protocol=%s, ticket=%s, validTime=%s,m userName=%s, userId=%s", super.toString(),
                graphicsType, getTicket(), getValidTime(), getUserName(), getUserId());
    }
}
