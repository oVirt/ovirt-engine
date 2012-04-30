package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public class DiscoverSendTargetsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public DiscoverSendTargetsVDSCommandParameters(Guid vdsId, storage_server_connections connection) {
        super(vdsId);
        setConnection(connection);
    }

    private storage_server_connections privateConnection;

    public storage_server_connections getConnection() {
        return privateConnection;
    }

    private void setConnection(storage_server_connections value) {
        privateConnection = value;
    }

    public DiscoverSendTargetsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, connection=%s", super.toString(), getConnection());
    }

}
