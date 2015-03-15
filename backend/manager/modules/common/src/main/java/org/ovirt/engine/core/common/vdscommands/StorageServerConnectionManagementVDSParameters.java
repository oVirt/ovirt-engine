package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionManagementVDSParameters extends GetStorageConnectionsListVDSCommandParameters {
    private StorageType privateStorageType;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    private List<StorageServerConnections> privateConnectionList;

    public List<StorageServerConnections> getConnectionList() {
        return privateConnectionList;
    }

    private void setConnectionList(List<StorageServerConnections> value) {
        privateConnectionList = value;
    }

    public StorageServerConnectionManagementVDSParameters(Guid vdsId, Guid storagePoolId, StorageType storageType,
            List<StorageServerConnections> connectionList) {
        super(vdsId, storagePoolId);
        setStorageType(storageType);
        setConnectionList(connectionList);
    }

    public StorageServerConnectionManagementVDSParameters() {
        privateStorageType = StorageType.UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("%s, storageType = %s, connectionList = [%s]",
                super.toString(), getStorageType().name(), getPrintableConnectionsList());
    }

    private String getPrintableConnectionsList() {
        StringBuilder sb = new StringBuilder();
        for (StorageServerConnections con : getConnectionList()) {
            sb.append(con);
        }
        return sb.toString();
    }
}
