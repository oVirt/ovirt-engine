package org.ovirt.engine.core.common.action;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class ConnectManagedBlockStorageDeviceCommandParameters extends ActionParametersBase {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConnectManagedBlockStorageDeviceCommandParameters)) {
            return false;
        }

        ConnectManagedBlockStorageDeviceCommandParameters that = (ConnectManagedBlockStorageDeviceCommandParameters) o;
        return Objects.equals(storageDomainId, that.storageDomainId) &&
                Objects.equals(connectorInfo, that.connectorInfo) &&
                Objects.equals(diskId, that.diskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageDomainId, connectorInfo, diskId);
    }
}
