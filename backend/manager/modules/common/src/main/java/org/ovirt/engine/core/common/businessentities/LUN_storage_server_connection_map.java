package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class LUN_storage_server_connection_map implements Serializable, BusinessEntity<LUN_storage_server_connection_map_id> {
    private static final long serialVersionUID = -4203034156149786569L;

    public LUN_storage_server_connection_map() {
    }

    public LUN_storage_server_connection_map(String lUN_id, String storage_server_connection) {
        this.id.lunId = lUN_id;
        this.id.storageServerConnection = storage_server_connection;
    }

    private LUN_storage_server_connection_map_id id = new LUN_storage_server_connection_map_id();

    @Override
    public LUN_storage_server_connection_map_id getId() {
        return this.id;
    }

    @Override
    public void setId(LUN_storage_server_connection_map_id value) {
        this.id = value;
    }

    public String getLunId() {
        return this.id.lunId;
    }

    public void setLunId(String value) {
        this.id.lunId = value;
    }

    public String getstorage_server_connection() {
        return this.id.storageServerConnection;
    }

    public void setstorage_server_connection(String value) {
        this.id.storageServerConnection = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.lunId == null) ? 0 : id.lunId.hashCode());
        result =
                prime * result
                        + ((id.storageServerConnection == null) ? 0 : id.storageServerConnection.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LUN_storage_server_connection_map other = (LUN_storage_server_connection_map) obj;
        if (id.lunId == null) {
            if (other.id.lunId != null)
                return false;
        } else if (!id.lunId.equals(other.id.lunId))
            return false;
        if (id.storageServerConnection == null) {
            if (other.id.storageServerConnection != null)
                return false;
        } else if (!id.storageServerConnection.equals(other.id.storageServerConnection))
            return false;
        return true;
    }
}
