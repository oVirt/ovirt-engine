package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class SetVmTicketVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String ticket;
    private int validTime;
    private String userName;
    private Guid userId;
    private GraphicsType graphicsType;
    private ConsoleDisconnectAction disconnectAction;
    private Version compatibilityVersion;
    private int consoleDisconnectActionDelay;

    public SetVmTicketVDSCommandParameters() {
    }

    public SetVmTicketVDSCommandParameters(Guid vdsId, Guid vmId, String ticket, int validTime, String userName,
                                           Guid userId, GraphicsType graphicsType, ConsoleDisconnectAction disconnectAction,
                                           Version compatibilityVersion, int consoleDisconnectActionDelay) {
        super(vdsId, vmId);
        this.ticket = ticket;
        this.validTime = validTime;
        this.userName = userName;
        this.userId = userId;
        this.graphicsType = graphicsType;
        this.disconnectAction = disconnectAction;
        this.compatibilityVersion = compatibilityVersion;
        this.consoleDisconnectActionDelay = consoleDisconnectActionDelay;
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

    public String getDisconnectAction() {
        return disconnectAction.name();
    }

    public GraphicsType getGraphicsType() {
        return graphicsType;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public int getConsoleDisconnectActionDelay() {
        return consoleDisconnectActionDelay;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("protocol", graphicsType)
                .append("ticket", getTicket())
                .append("validTime", getValidTime())
                .append("userName", getUserName())
                .append("userId", getUserId())
                .append("disconnectAction", getDisconnectAction())
                .append("consoleDisconnectActionDelay", getConsoleDisconnectActionDelay());
    }
}
