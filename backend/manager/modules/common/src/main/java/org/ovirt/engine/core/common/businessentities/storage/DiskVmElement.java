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

    private boolean passDiscard;

    private DiskInterface diskInterface;

    private boolean usingScsiReservation;

    /**
     * This field is transient and is taken from the corresponding VM device.
     * It is used solely for UI/API purposes and is not persisted or updated through DiskVmElement.
     */
    @TransientField
    private boolean plugged;

    /**
     * This field is transient and is taken from the corresponding VM device.
     * It is used solely for UI/API purposes and is not persisted or updated through DiskVmElement.
     */
    @TransientField
    private String logicalName;

    /**
     * This field is transient and is taken from the corresponding VM device.
     * It is used solely for UI/API purposes and is not persisted or updated through DiskVmElement.
     */
    @TransientField
    private boolean readOnly;

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

    public boolean isPassDiscard() {
        return passDiscard;
    }

    public void setPassDiscard(boolean passDiscard) {
        this.passDiscard = passDiscard;
    }

    @NotNull(message = "VALIDATION_DISK_INTERFACE_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    public DiskInterface getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(DiskInterface diskInterface) {
        this.diskInterface = diskInterface;
    }

    public boolean isUsingScsiReservation() {
        return usingScsiReservation;
    }

    public void setUsingScsiReservation(boolean usingScsiReservation) {
        this.usingScsiReservation = usingScsiReservation;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
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
        dest.setPassDiscard(source.isPassDiscard());
        dest.setDiskInterface(source.getDiskInterface());
        dest.setUsingScsiReservation(source.isUsingScsiReservation());
        dest.setPlugged(source.isPlugged());
        dest.setLogicalName(source.getLogicalName());
        dest.setReadOnly(source.isReadOnly());
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
                passDiscard == that.passDiscard &&
                diskInterface == that.diskInterface &&
                usingScsiReservation == that.usingScsiReservation &&
                plugged == that.plugged &&
                Objects.equals(logicalName, that.logicalName) &&
                readOnly == that.readOnly &&
                Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, boot, passDiscard, diskInterface, usingScsiReservation, plugged,
                logicalName, readOnly);
    }
}
