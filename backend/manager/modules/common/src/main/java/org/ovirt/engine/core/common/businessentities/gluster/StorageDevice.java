package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/*
 * StorageDevice represents storage devices attached the hosts. It can be a disk or partition or pvs, lvs, etc.
 */
public class StorageDevice implements Queryable, BusinessEntity<Guid>, Nameable {

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
    private boolean isGlusterBrick;

    @Override
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
        return Objects.hash(
                canCreateBrick,
                isGlusterBrick,
                description,
                devPath,
                devType,
                devUuid,
                fsType,
                fsUuid,
                id,
                mountPoint,
                name,
                size,
                vdsId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageDevice)) {
            return false;
        }
        StorageDevice storageDevice = (StorageDevice) obj;
        return Objects.equals(id, storageDevice.id)
                && canCreateBrick == storageDevice.canCreateBrick
                && isGlusterBrick == storageDevice.isGlusterBrick
                && Objects.equals(description, storageDevice.description)
                && Objects.equals(devPath, storageDevice.devPath)
                && Objects.equals(devType, storageDevice.devType)
                && Objects.equals(devUuid, storageDevice.devUuid)
                && Objects.equals(fsType, storageDevice.fsType)
                && Objects.equals(fsUuid, storageDevice.fsUuid)
                && Objects.equals(mountPoint, storageDevice.mountPoint)
                && Objects.equals(name, storageDevice.name)
                && size == storageDevice.size
                && Objects.equals(vdsId, storageDevice.vdsId);
    }

    public boolean isGlusterBrick() {
        return isGlusterBrick;
    }

    public void setGlusterBrick(boolean isGlusterBrick) {
        this.isGlusterBrick = isGlusterBrick;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("name", name)
                .append("devUuid", devUuid)
                .append("fsUuid", fsUuid)
                .append("vdsId", vdsId)
                .append("description", description)
                .append("devType", devType)
                .append("devPath", devPath)
                .append("fsType", fsType)
                .append("mountPoint", mountPoint)
                .append("size", size)
                .append("canCreateBrick", canCreateBrick)
                .append("isGlusterBrick", isGlusterBrick)
                .build();
    }
}
