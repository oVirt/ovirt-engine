package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DiscoverSendTargetsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public DiscoverSendTargetsVDSCommandParameters(Guid vdsId, StorageServerConnections connection) {
        super(vdsId);
        setConnection(connection);
    }

    private StorageServerConnections privateConnection;

    public StorageServerConnections getConnection() {
        return privateConnection;
    }

    private void setConnection(StorageServerConnections value) {
        privateConnection = value;
    }

    public DiscoverSendTargetsVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("connection", getConnection());
    }
}
