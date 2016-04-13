package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetImageTransferSessionStatsVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    public GetImageTransferSessionStatsVDSCommandParameters(Guid vdsId, Guid ticketId) {
        super(vdsId, ticketId, null);
    }

    public GetImageTransferSessionStatsVDSCommandParameters() {
    }
}
