package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    public GetImageTicketVDSCommandParameters(Guid vdsId, Guid ticketId) {
        super(vdsId, ticketId, null);
    }

    public GetImageTicketVDSCommandParameters() {
    }
}
