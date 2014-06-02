package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.compat.Guid;

public class SetVmTicketParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 7066467049851637162L;

    private String ticket;
    private int validTime;
    private String clientIp;
    private GraphicsType graphicsType;

    public SetVmTicketParameters() {
    }

    public SetVmTicketParameters(Guid vmId, String ticket, int validTime, GraphicsType graphicsType) {
        super(vmId);
        this.graphicsType = graphicsType;
        this.ticket = ticket;
        this.validTime = validTime;
    }

    public SetVmTicketParameters(Guid vmId, String ticket, int validTime, GraphicsType graphicsType,  String clientIp) {
        this(vmId, ticket, validTime, graphicsType);
        this.clientIp = clientIp;
    }

    public String getTicket() {
        return ticket;
    }

    public int getValidTime() {
        return validTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public GraphicsType getGraphicsType() {
        return graphicsType;
    }

}
