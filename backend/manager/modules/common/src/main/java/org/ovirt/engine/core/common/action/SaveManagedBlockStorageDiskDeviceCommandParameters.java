package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class SaveManagedBlockStorageDiskDeviceCommandParameters extends ActionParametersBase {
    private static final long serialVersionUID = -2206597938664239605L;
    private Guid storageDomainId;
    private Map<String, Object> device;
    private Guid diskId;

    public SaveManagedBlockStorageDiskDeviceCommandParameters() {
    }

    public SaveManagedBlockStorageDiskDeviceCommandParameters(String engineSessionId,
            Guid storageDomainId, Map<String, Object> device, Guid diskId) {
        super(engineSessionId);
        this.storageDomainId = storageDomainId;
        this.device = device;
        this.diskId = diskId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Map<String, Object> getDevice() {
        return device;
    }

    public void setDevice(Map<String, Object> device) {
        this.device = device;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }
}
