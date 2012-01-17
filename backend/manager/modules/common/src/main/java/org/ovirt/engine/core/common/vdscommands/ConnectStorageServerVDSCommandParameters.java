package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ConnectStorageServerVDSCommandParameters")
public class ConnectStorageServerVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    @XmlElement(name = "StorageType")
    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    @XmlElement(name = "ConnectionList")
    private List<storage_server_connections> privateConnectionList;

    public List<storage_server_connections> getConnectionList() {
        return privateConnectionList;
    }

    private void setConnectionList(List<storage_server_connections> value) {
        privateConnectionList = value;
    }

    public ConnectStorageServerVDSCommandParameters(Guid vdsId, Guid storagePoolId, StorageType storageType,
            List<storage_server_connections> connectionList) {
        super(vdsId, storagePoolId);
        setStorageType(storageType);
        setConnectionList(connectionList);
    }

    public ConnectStorageServerVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageType = %s, connectionList = [%s]",
                super.toString(), getStorageType().name(), getPrintableConnectionsList());
    }

    private String getPrintableConnectionsList() {
        StringBuilder sb = new StringBuilder();
        for (storage_server_connections con : getConnectionList()) {
            sb.append("{ id: ");
            sb.append(con.getid());
            sb.append(", connection: ");
            sb.append(con.getconnection());
            sb.append(" };");
        }
        return sb.toString();
    }
}
