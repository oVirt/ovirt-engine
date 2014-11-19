package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class StorageDeviceListReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String DEVICE_INFO = "deviceInfo";
    private static final String NAME = "name";
    private static final String FILE_SYSTEM_TYPE = "fsType";
    private static final String SIZE = "size";
    private static final String MOUNT_POINT = "mountPoint";
    private static final String DEV_PATH = "devPath";
    private static final String DEV_UUID = "devUuid";
    private static final String UUID = "uuid";
    private static final String CREATE_BRICK = "createBrick";
    private static final String MODEL = "model";
    private static final String BUS = "bus";

    private List<StorageDevice> storageDevices;

    @SuppressWarnings("unchecked")
    public StorageDeviceListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (mStatus.mCode != 0) {
            return;
        }

        Object[] deviceArray = (Object[]) innerMap.get(DEVICE_INFO);
        storageDevices = new ArrayList<StorageDevice>();

        if (deviceArray != null) {
            for (Object deviceInfoMap : deviceArray) {
                storageDevices.add(getStorageDevice((Map<String, Object>) deviceInfoMap));
            }
        }
    }

    private StorageDevice getStorageDevice(Map<String, Object> map) {
        StorageDevice storageDevice = new StorageDevice();
        storageDevice.setName(map.get(NAME).toString());
        storageDevice.setSize((long) (map.containsKey(SIZE) ? Double.valueOf(map.get(SIZE).toString()) : 0));
        storageDevice.setDevPath(map.containsKey(DEV_PATH) ? map.get(DEV_PATH).toString() : null);
        storageDevice.setCanCreateBrick(map.containsKey(CREATE_BRICK) ? Boolean.valueOf(map.get(CREATE_BRICK).toString()) : Boolean.FALSE);
        storageDevice.setFsType(map.containsKey(FILE_SYSTEM_TYPE)
                && StringUtils.isNotBlank(map.get(FILE_SYSTEM_TYPE).toString()) ? map.get(FILE_SYSTEM_TYPE)
                .toString()
                : null);
        storageDevice.setMountPoint(map.containsKey(MOUNT_POINT)
                && StringUtils.isNotBlank(map.get(MOUNT_POINT).toString()) ? map.get(MOUNT_POINT)
                .toString()
                : null);
        storageDevice.setDevUuid(map.containsKey(DEV_UUID) && StringUtils.isNotBlank(map.get(DEV_UUID).toString()) ? map.get(DEV_UUID)
                .toString()
                : null);
        storageDevice.setFsUuid(map.containsKey(UUID) && StringUtils.isNotBlank(map.get(UUID).toString()) ? map.get(UUID)
                .toString()
                : null);
        storageDevice.setDescription(map.containsKey(MODEL) ? map.get(MODEL).toString() : null);
        storageDevice.setDevType(map.containsKey(BUS) && StringUtils.isNotBlank(map.get(BUS).toString()) ? map.get(BUS)
                .toString()
                : null);
        return storageDevice;
    }

    public List<StorageDevice> getStorageDevices() {
        return storageDevices;
    }

}
