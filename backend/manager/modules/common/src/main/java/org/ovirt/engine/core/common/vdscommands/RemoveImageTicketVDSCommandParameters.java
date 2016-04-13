package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RemoveImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    public RemoveImageTicketVDSCommandParameters(Guid vdsId, Guid ticketId) {
        super(vdsId, ticketId, null);
    }

    public RemoveImageTicketVDSCommandParameters() {}
}
