package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS Command parameters class for the "Create Brick" action, with the list of Storage Devices
 */
public class CreateBrickVDSParameters extends VdsIdVDSCommandParametersBase {

    private String lvName;
    private String mountPoint;
    private List<StorageDevice> storageDevices;
    private Map<String, Object> raidParams;
    private String fsType;

    public CreateBrickVDSParameters() {

    }

    public CreateBrickVDSParameters(Guid hostId,
            String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            List<StorageDevice> storageDevices) {
        super(hostId);
        this.lvName = lvName;
        this.mountPoint = mountPoint;
        this.setStorageDevices(storageDevices);
        this.raidParams = raidParams;
        this.fsType = fsType;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public List<StorageDevice> getStorageDevices() {
        return storageDevices;
    }

    public void setStorageDevices(List<StorageDevice> storageDevices) {
        this.storageDevices = storageDevices;
    }

    public Map<String, Object> getRaidParams() {
        return raidParams;
    }

    public void setRaidParams(Map<String, Object> raidParams) {
        this.raidParams = raidParams;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getFsType() {
        return fsType;
    }

    public void setFsType(String fsType) {
        this.fsType = fsType;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("lvName", lvName)
                .append("mountPoint", mountPoint)
                .append("storageDevices", storageDevices)
                .append("raidParams", raidParams)
                .append("fsType", fsType);
    }
}
