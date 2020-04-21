package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;


public class AddImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    private ImageTicket ticket;

    public AddImageTicketVDSCommandParameters() {
    }

    public AddImageTicketVDSCommandParameters(Guid vdsId, ImageTicket ticket) {
        super(vdsId, ticket.getId(), (long) ticket.getTimeout());
        this.ticket = ticket;
    }

    public ImageTicket getImageTicket() {
        return ticket;
    }

    public void setImageTicket(ImageTicket ticket) {
        this.ticket = ticket;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("operations", ticket.getOps())
                .append("size", ticket.getSize())
                .append("url", ticket.getUrl())
                .append("filename", ticket.getFilename())
                .append("sparse", ticket.isSparse())
                .append("transferId", ticket.getTransferId())
                .append("dirty", ticket.isDirty());
    }
}
