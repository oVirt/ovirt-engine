package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class StorageServerConnectionParametersBase extends StoragePoolParametersBase {
    private static final long serialVersionUID = 1516671099713952453L;
    private storage_server_connections privateStorageServerConnection;

    public storage_server_connections getStorageServerConnection() {
        return privateStorageServerConnection;
    }

    public void setStorageServerConnection(storage_server_connections value) {
        privateStorageServerConnection = value;
    }

    public StorageServerConnectionParametersBase(storage_server_connections connection, Guid vdsId) {
        super(Guid.Empty);
        setStorageServerConnection(connection);
        setVdsId(vdsId);
    }

    public StorageServerConnectionParametersBase() {
    }
}
