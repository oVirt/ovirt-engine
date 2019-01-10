package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class ConnectManagedBlockStorageDeviceCommandParameters extends ActionParametersBase {
    private static final long serialVersionUID = 8073878919797807510L;

    private Guid storageDomainId;
    private Map<String, Object> connectorInfo;
    private Guid diskId;

    public ConnectManagedBlockStorageDeviceCommandParameters() {
    }

    public ConnectManagedBlockStorageDeviceCommandParameters(Guid storageDomainId,
            Map<String, Object> connectorInfo, Guid diskId) {
        this.storageDomainId = storageDomainId;
        this.connectorInfo = connectorInfo;
        this.diskId = diskId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Map<String, Object> getConnectorInfo() {
        return connectorInfo;
    }

    public void setConnectorInfo(Map<String, Object> connectorInfo) {
        this.connectorInfo = connectorInfo;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }
}
