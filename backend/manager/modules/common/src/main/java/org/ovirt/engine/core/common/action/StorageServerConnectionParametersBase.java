package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = 6389650711081394484L;

    @Valid
    private StorageServerConnections privateStorageServerConnection;

    private Guid vdsId;

    public StorageServerConnectionParametersBase(StorageServerConnections connection, Guid vdsId) {
        setStorageServerConnection(connection);
        setVdsId(vdsId);
    }

    public StorageServerConnectionParametersBase() {
    }

    public StorageServerConnections getStorageServerConnection() {
        return privateStorageServerConnection;
    }

    public void setStorageServerConnection(StorageServerConnections value) {
        privateStorageServerConnection = value;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}
