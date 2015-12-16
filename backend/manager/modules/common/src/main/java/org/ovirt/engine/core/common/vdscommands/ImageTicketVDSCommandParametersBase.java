package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ImageTicketVDSCommandParametersBase extends VdsIdVDSCommandParametersBase {
    private Guid ticketId;
    private Long timeout;

    public ImageTicketVDSCommandParametersBase(Guid vdsId, Guid ticketId, Long timeout) {
        super(vdsId);
        this.ticketId = ticketId;
        this.timeout = timeout;
    }

    public ImageTicketVDSCommandParametersBase() {}

    public Guid getTicketId() {
        return ticketId;
    }

    public Long getTimeout() {
        return timeout;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("ticketId", getTicketId())
                .append("timeout", getTimeout());
    }
}
