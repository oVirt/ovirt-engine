package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ManagedBlockStorageDisk extends DiskImage {
    private static final long serialVersionUID = -6032118718251114213L;
    public static final String ATTACHED_VDS_ID = "attached_vds_id";
    public static final String DEST_VDS_ID = "dest_vds_id";
    public static String DRIVER_VOLUME_TYPE = "driver_volume_type";

    private Map<String, Object> device;

    private Map<String, Object> connectionInfo;

    private CinderVolumeDriver cinderVolumeDriver;

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.MANAGED_BLOCK_STORAGE;
    }

    public ManagedBlockStorageDisk() {
    }

    public ManagedBlockStorageDisk(DiskImage diskImage) {
        super(diskImage);
        setStorageTypes(Arrays.asList(StorageType.MANAGED_BLOCK_STORAGE));
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

    public CinderVolumeDriver getCinderVolumeDriver() {
        return cinderVolumeDriver;
    }

    public void setCinderVolumeDriver(CinderVolumeDriver cinderVolumeDriver) {
        this.cinderVolumeDriver = cinderVolumeDriver;
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
                Objects.equals(connectionInfo, that.connectionInfo) &&
                cinderVolumeDriver == that.cinderVolumeDriver;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), device, connectionInfo, cinderVolumeDriver);
    }
}
