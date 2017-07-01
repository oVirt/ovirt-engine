package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class DiscoverSendTargetsQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 5247364599284007838L;
    private StorageServerConnections privateConnection;

    public StorageServerConnections getConnection() {
        return privateConnection;
    }

    private void setConnection(StorageServerConnections value) {
        privateConnection = value;
    }

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public DiscoverSendTargetsQueryParameters(Guid vdsId, StorageServerConnections connection) {
        setVdsId(vdsId);
        setConnection(connection);
    }

    public DiscoverSendTargetsQueryParameters() {
    }
}
