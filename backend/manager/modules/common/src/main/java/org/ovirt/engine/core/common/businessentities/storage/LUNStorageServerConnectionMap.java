package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

public class LUNStorageServerConnectionMap implements Serializable, BusinessEntity<LUNStorageServerConnectionMapId> {
    private static final long serialVersionUID = -4203034156149786569L;

    public LUNStorageServerConnectionMap() {
        id = new LUNStorageServerConnectionMapId();
    }

    public LUNStorageServerConnectionMap(String lUN_id, String storage_server_connection) {
        this();
        this.id.lunId = lUN_id;
        this.id.storageServerConnection = storage_server_connection;
    }

    private LUNStorageServerConnectionMapId id;

    @Override
    public LUNStorageServerConnectionMapId getId() {
        return this.id;
    }

    @Override
    public void setId(LUNStorageServerConnectionMapId value) {
        this.id = value;
    }

    public String getLunId() {
        return this.id.lunId;
    }

    public void setLunId(String value) {
        this.id.lunId = value;
    }

    public String getStorageServerConnection() {
        return this.id.storageServerConnection;
    }

    public void setStorageServerConnection(String value) {
        this.id.storageServerConnection = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id.lunId,
                id.storageServerConnection
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LUNStorageServerConnectionMap)) {
            return false;
        }
        LUNStorageServerConnectionMap other = (LUNStorageServerConnectionMap) obj;
        return Objects.equals(id.lunId, other.id.lunId)
                && Objects.equals(id.storageServerConnection, other.id.storageServerConnection);
    }
}
