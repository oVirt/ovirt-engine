package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

/*
 * StorageDevice represents storage devices attached the hosts. It can be a disk or partition or pvs, lvs, etc.
 */
public class StorageDevice extends IVdcQueryable implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -1613957987974435240L;

    private Guid id;
    private String name;
    private String devUuid;
    private String fsUuid;
    private Guid vdsId;
    private String description;
    private String devType;
    private String devPath;
    private String fsType;
    private String mountPoint;
    private long size;
    private boolean canCreateBrick;

    public String getName() {
        return name;
    }

    public String getDevPath() {
        return devPath;
    }

    public String getDevUuid() {
        return devUuid;
    }

    public String getFsUuid() {
        return fsUuid;
    }

    public boolean getCanCreateBrick() {
        return canCreateBrick;
    }

    public String getFsType() {
        return fsType;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public long getSize() {
        return size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDevPath(String devPath) {
        this.devPath = devPath;
    }

    public void setDevUuid(String devUuid) {
        this.devUuid = devUuid;
    }

    public void setFsUuid(String uuid) {
        this.fsUuid = uuid;
    }

    public void setCanCreateBrick(boolean canCreateBrick) {
        this.canCreateBrick = canCreateBrick;
    }

    public void setFsType(String fsType) {
        this.fsType = fsType;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String model) {
        this.description = model;
    }

    @Override
    public Object getQueryableId() {
        return this.getId();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (canCreateBrick ? 0 : 1);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((devPath == null) ? 0 : devPath.hashCode());
        result = prime * result + ((devType == null) ? 0 : devType.hashCode());
        result = prime * result + ((devUuid == null) ? 0 : devUuid.hashCode());
        result = prime * result + ((fsType == null) ? 0 : fsType.hashCode());
        result = prime * result + ((fsUuid == null) ? 0 : fsUuid.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((mountPoint == null) ? 0 : mountPoint.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof StorageDevice)) {
            StorageDevice storageDevice = (StorageDevice) obj;
            if (ObjectUtils.objectsEqual(getId(), storageDevice.getId())
                    && canCreateBrick == storageDevice.canCreateBrick
                    && (ObjectUtils.objectsEqual(getDescription(), storageDevice.getDescription()))
                    && (ObjectUtils.objectsEqual(getDevPath(), storageDevice.getDevPath()))
                    && (ObjectUtils.objectsEqual(getDevType(), storageDevice.getDevType()))
                    && (ObjectUtils.objectsEqual(getDevUuid(), storageDevice.getDevUuid()))
                    && (ObjectUtils.objectsEqual(getFsType(), storageDevice.getFsType()))
                    && (ObjectUtils.objectsEqual(getFsUuid(), storageDevice.getFsUuid()))
                    && (ObjectUtils.objectsEqual(getMountPoint(), storageDevice.getMountPoint()))
                    && (ObjectUtils.objectsEqual(getName(), storageDevice.getName()))
                    && size == storageDevice.size
                    && (ObjectUtils.objectsEqual(getVdsId(), storageDevice.getVdsId()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "StorageDevice [id=" + id + ", name=" + name + ", devUuid=" + devUuid + ", fsUuid=" + fsUuid
                + ", vdsId=" + vdsId + ", description=" + description + ", devType=" + devType + ", devPath=" + devPath
                + ", fsType=" + fsType + ", mountPoint=" + mountPoint + ", size=" + size + ", canCreateBrick="
                + canCreateBrick + "]";
    }
}
