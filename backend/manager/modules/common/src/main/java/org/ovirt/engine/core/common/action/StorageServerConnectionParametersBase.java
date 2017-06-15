package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = 6389650711081394484L;

    @Valid
    private StorageServerConnections privateStorageServerConnection;

    private Guid vdsId;

    private boolean force;

    public StorageServerConnectionParametersBase(StorageServerConnections connection, Guid vdsId, boolean force) {
        setStorageServerConnection(connection);
        setVdsId(vdsId);
        setForce(force);
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

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
