package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionParametersBase extends StoragePoolParametersBase {
    private static final long serialVersionUID = 1516671099713952453L;

    @Valid
    private StorageServerConnections privateStorageServerConnection;

    public StorageServerConnections getStorageServerConnection() {
        return privateStorageServerConnection;
    }

    public void setStorageServerConnection(StorageServerConnections value) {
        privateStorageServerConnection = value;
    }

    public StorageServerConnectionParametersBase(StorageServerConnections connection, Guid vdsId) {
        super(Guid.Empty);
        setStorageServerConnection(connection);
        setVdsId(vdsId);
    }

    public StorageServerConnectionParametersBase() {
    }
}
