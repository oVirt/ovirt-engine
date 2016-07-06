package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.TransientField;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class DiskVmElement implements BusinessEntity<VmDeviceId> {
    /**
     * The vm device id of the disk vm element, this will be consisted of the disk id along with the vm id
     */
    private VmDeviceId id;

    private boolean boot;

    private DiskInterface diskInterface;

    /**
     * This field is transient and is taken from the corresponding VM device.
     * It is used solely for UI/API purposes and is not persisted or updated through DiskVmElement.
     */
    @TransientField
    private boolean plugged;

    public DiskVmElement() {
    }

    public DiskVmElement(VmDeviceId id) {
        setId(id);
    }

    public DiskVmElement(Guid diskId, Guid vmId) {
        setId(new VmDeviceId(diskId, vmId));
    }

    public VmDeviceId getId() {
        return id;
    }

    public void setId(VmDeviceId id) {
        this.id = id;
    }

    public boolean isBoot() {
        return boot;
    }

    public void setBoot(boolean boot) {
        this.boot = boot;
    }

    @NotNull(message = "VALIDATION_DISK_INTERFACE_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    public DiskInterface getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(DiskInterface diskInterface) {
        this.diskInterface = diskInterface;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    public Guid getDiskId() {
        return getId().getDeviceId();
    }

    public Guid getVmId() {
        return getId().getVmId();
    }

    public static DiskVmElement copyOf(DiskVmElement diskVmElement) {
        DiskVmElement newDve = new DiskVmElement(diskVmElement.getId().getDeviceId(), diskVmElement.getId().getVmId());
        copyProperties(diskVmElement, newDve);
        return newDve;
    }

    public static DiskVmElement copyOf(DiskVmElement diskVmElement, Guid diskId, Guid vmId) {
        DiskVmElement newDve = new DiskVmElement(diskId, vmId);
        copyProperties(diskVmElement, newDve);
        return newDve;
    }

    private static void copyProperties(DiskVmElement source, DiskVmElement dest) {
        dest.setBoot(source.isBoot());
        dest.setDiskInterface(source.getDiskInterface());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiskVmElement that = (DiskVmElement) o;
        return boot == that.boot &&
                diskInterface == that.diskInterface &&
                id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, boot, diskInterface);
    }
}
