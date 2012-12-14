package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
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
    public String toString() {
        return String.format("%s, connection=%s", super.toString(), getConnection());
    }

}
