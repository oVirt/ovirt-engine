package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainsAndStoragePoolIdQueryParameters extends IdQueryParameters {
    private List<StorageDomain> storageDomainList;
    private StorageServerConnections storageServerConnection;
    private Guid vdsId;
    private boolean checkStoragePoolStatus = true;

    public StorageDomainsAndStoragePoolIdQueryParameters() {
    }

    public StorageDomainsAndStoragePoolIdQueryParameters(List<StorageDomain> storageDomainList,
            Guid storagePoolId,
            Guid vdsId) {
        super(storagePoolId);
        this.storageDomainList = storageDomainList;
        this.vdsId = vdsId;
    }

    public StorageDomainsAndStoragePoolIdQueryParameters(List<StorageDomain> storageDomainList, Guid storagePoolId) {
        this(storageDomainList, storagePoolId, null);
    }

    public StorageDomainsAndStoragePoolIdQueryParameters(StorageDomain storageDomain, Guid storagePoolId, Guid vdsId) {
        super(storagePoolId);
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        this.storageDomainList = storageDomainList;
        this.vdsId = vdsId;
    }

    public StorageDomainsAndStoragePoolIdQueryParameters(StorageDomain storageDomain,
            Guid storagePoolId,
            Guid vdsId,
            boolean checkStoragePoolStatus) {
        this(storageDomain, storagePoolId, vdsId);
        this.checkStoragePoolStatus = checkStoragePoolStatus;
    }

    public StorageDomainsAndStoragePoolIdQueryParameters(StorageServerConnections storageServerConnection,
            Guid storagePoolId,
            Guid vdsId) {
        super(storagePoolId);
        this.storageServerConnection = storageServerConnection;
        this.vdsId = vdsId;
    }

    public List<StorageDomain> getStorageDomainList() {
        return storageDomainList;
    }

    public void setStorageDomainIdList(List<StorageDomain> storageDomainList) {
        this.storageDomainList = storageDomainList;
    }

    public StorageServerConnections getStorageServerConnection() {
        return storageServerConnection;
    }

    public void setStorageServerConnection(StorageServerConnections storageServerConnection) {
        this.storageServerConnection = storageServerConnection;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public boolean isCheckStoragePoolStatus() {
        return checkStoragePoolStatus;
    }

    public void setCheckStoragePoolStatus(boolean checkStoragePoolStatus) {
        this.checkStoragePoolStatus = checkStoragePoolStatus;
    }
}
