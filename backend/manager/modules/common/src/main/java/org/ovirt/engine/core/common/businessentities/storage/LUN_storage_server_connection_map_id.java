package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;

public class LUN_storage_server_connection_map_id implements Serializable {
    private static final long serialVersionUID = -1212805426968232766L;

    public String lunId;

    public String storageServerConnection;

    public LUN_storage_server_connection_map_id() {
    }

    public LUN_storage_server_connection_map_id(String lunId, String storageServerConnection) {
        this.lunId = lunId;
        this.storageServerConnection = storageServerConnection;
    }
}
