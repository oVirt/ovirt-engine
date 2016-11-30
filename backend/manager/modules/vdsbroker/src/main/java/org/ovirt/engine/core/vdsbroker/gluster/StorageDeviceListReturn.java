package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;

public class StorageDeviceListReturn extends StorageDeviceReturn {

    private static final String DEVICE_INFO = "deviceInfo";
    private List<StorageDevice> storageDevices;

    @SuppressWarnings("unchecked")
    public StorageDeviceListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (getStatus().code != 0) {
            return;
        }

        Object[] deviceArray = (Object[]) innerMap.get(DEVICE_INFO);
        storageDevices = new ArrayList<>();

        if (deviceArray != null) {
            for (Object deviceInfoMap : deviceArray) {
                storageDevices.add(getStorageDevice((Map<String, Object>) deviceInfoMap));
            }
        }
    }

    public List<StorageDevice> getStorageDevices() {
        return storageDevices;
    }

}
