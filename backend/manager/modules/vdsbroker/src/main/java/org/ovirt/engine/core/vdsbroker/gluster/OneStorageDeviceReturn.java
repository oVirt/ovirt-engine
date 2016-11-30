package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;

public class OneStorageDeviceReturn extends StorageDeviceReturn {
    private static final String DEVICE = "device";

    private StorageDevice storageDevice;

    @SuppressWarnings("unchecked")
    public OneStorageDeviceReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (getStatus().code != 0) {
            return;
        }
        if (innerMap.containsKey(DEVICE)) {
            this.storageDevice = getStorageDevice((Map<String, Object>) innerMap.get(DEVICE));
        }
    }

    public StorageDevice getStorageDevice() {
        return storageDevice;
    }

}
