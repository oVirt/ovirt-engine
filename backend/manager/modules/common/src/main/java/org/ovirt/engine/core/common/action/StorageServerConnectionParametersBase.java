package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionParametersBase extends StoragePoolParametersBase {
    private static final long serialVersionUID = 1516671099713952453L;

    @Valid
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
