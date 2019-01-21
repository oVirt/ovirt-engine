package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.Objects;

public class ManagedBlockStorageDisk extends DiskImage {
    private static final long serialVersionUID = -949344024751821482L;
    public static final String ATTACHED_VDS_ID = "attached_vds_id";

    private Map<String, Object> device;

    private Map<String, Object> connectionInfo;

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.MANAGED_BLOCK_STORAGE;
    }

    public ManagedBlockStorageDisk() {
    }

    public Map<String, Object> getDevice() {
        return device;
    }

    public void setDevice(Map<String, Object> device) {
        this.device = device;
    }

    public Map<String, Object> getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(Map<String, Object> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManagedBlockStorageDisk)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ManagedBlockStorageDisk that = (ManagedBlockStorageDisk) o;
        return Objects.equals(device, that.device) &&
                Objects.equals(connectionInfo, that.connectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), device, connectionInfo);
    }
}
