package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class DisconnectManagedBlockStorageDeviceParameters extends ActionParametersBase {
    private static final long serialVersionUID = 8103563657291725819L;
    private Guid storageDomainId;
    private Map<String, Object> connectionInfo;
    private Guid diskId;
    private Guid vdsId;

    public DisconnectManagedBlockStorageDeviceParameters(Guid storageDomainId,
            Map<String, Object> connectionInfo, Guid diskId, Guid vdsId) {
        this.storageDomainId = storageDomainId;
        this.connectionInfo = connectionInfo;
        this.diskId = diskId;
        this.vdsId = vdsId;
    }

    public DisconnectManagedBlockStorageDeviceParameters() {
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Map<String, Object> getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(Map<String, Object> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}
