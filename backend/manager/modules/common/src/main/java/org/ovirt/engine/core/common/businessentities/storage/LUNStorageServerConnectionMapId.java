package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;

public class LUNStorageServerConnectionMapId implements Serializable {
    private static final long serialVersionUID = -1212805426968232766L;

    public String lunId;

    public String storageServerConnection;

    public LUNStorageServerConnectionMapId() {
    }

    public LUNStorageServerConnectionMapId(String lunId, String storageServerConnection) {
        this.lunId = lunId;
        this.storageServerConnection = storageServerConnection;
    }
}
