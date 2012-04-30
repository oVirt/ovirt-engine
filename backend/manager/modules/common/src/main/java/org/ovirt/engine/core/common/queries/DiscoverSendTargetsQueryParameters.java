package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class DiscoverSendTargetsQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 5247364599284007838L;
    private storage_server_connections privateConnection;

    public storage_server_connections getConnection() {
        return privateConnection;
    }

    private void setConnection(storage_server_connections value) {
        privateConnection = value;
    }

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public DiscoverSendTargetsQueryParameters(Guid vdsId, storage_server_connections connection) {
        setVdsId(vdsId);
        setConnection(connection);
    }

    public DiscoverSendTargetsQueryParameters() {
    }
}
