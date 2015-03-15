package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ObjectUtils;

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
        result = prime * result
                + ((id.storageServerConnection == null) ? 0 : id.storageServerConnection.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LUNStorageServerConnectionMap other = (LUNStorageServerConnectionMap) obj;
        return (ObjectUtils.objectsEqual(id.lunId, other.id.lunId)
                && ObjectUtils.objectsEqual(id.storageServerConnection, other.id.storageServerConnection));
    }
}
