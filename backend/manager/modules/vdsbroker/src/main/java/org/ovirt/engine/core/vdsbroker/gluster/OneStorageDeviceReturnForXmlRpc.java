package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;

public class OneStorageDeviceReturnForXmlRpc extends StorageDeviceReturnForXmlRpc {
    private static final String DEVICE = "device";

    private StorageDevice storageDevice;

    @SuppressWarnings("unchecked")
    public OneStorageDeviceReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (getXmlRpcStatus().code != 0) {
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
