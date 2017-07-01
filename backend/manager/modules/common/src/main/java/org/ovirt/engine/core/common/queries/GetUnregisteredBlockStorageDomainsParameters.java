package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredBlockStorageDomainsParameters extends QueryParametersBase {
    private static final long serialVersionUID = 6989522172841845637L;

    private Guid vdsId;
    private StorageType storageType;
    private List<StorageServerConnections> storageServerConnections;

    public GetUnregisteredBlockStorageDomainsParameters() {
    }

    public GetUnregisteredBlockStorageDomainsParameters(Guid vdsId, StorageType storageType) {
        this(vdsId, storageType, null);
    }

    public GetUnregisteredBlockStorageDomainsParameters(Guid vdsId, StorageType storageType, List<StorageServerConnections> storageServerConnections) {
        this.vdsId = vdsId;
        this.storageType = storageType;
        this.storageServerConnections = storageServerConnections;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public List<StorageServerConnections> getStorageServerConnections() {
        return storageServerConnections;
    }

    public void setStorageServerConnections(List<StorageServerConnections> storageServerConnections) {
        this.storageServerConnections = storageServerConnections;
    }
}
