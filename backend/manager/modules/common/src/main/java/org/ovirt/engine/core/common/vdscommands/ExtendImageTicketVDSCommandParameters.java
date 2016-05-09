package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ExtendImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    public ExtendImageTicketVDSCommandParameters(Guid vdsId,
            Guid ticketId,
            long timeout) {
        super(vdsId, ticketId, timeout);
    }

    public ExtendImageTicketVDSCommandParameters() {}
}
