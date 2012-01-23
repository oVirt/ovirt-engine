package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * The disk represents a drive in the VM/Template.<br>
 * <br>
 * The disk data consists of this entity which holds the immutable fields of the drive, and the DiskImage entity which
 * represents the drive's actual data, and contains the mutable fields.<br>
 * Each drive can have several of these "images" associated to it, which represent the drive's snapshots - a backup of
 * the drive's data at a certain point in time. An image of a snapshot is immutable, and there is usually (in case of a
 * VM) one or more mutable images which the VM can run with.<br>
 * <br>
 * Due to this, the {@link Disk} entity always points to the active mutable image that the VM will run with (or the
 * image the Template represents).<br>
 * The active image can also be <code>null</code>, in case that it's missing but should be there.
 */
public class Disk extends IVdcQueryable implements BusinessEntity<Guid> {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 5883196978129104663L;

    /**
     * The disk ID uniquely identifies a disk in the system.
     */
    private Guid id;

    /**
     * The order of the drive in the VM/Template.
     */
    private int internalDriveMapping;

    /**
     * The type of disk (Data/System/etc).
     */
    private DiskType diskType;

    /**
     * The disk interface (IDE/SCSI/etc).
     */
    private DiskInterface diskInterface;

    /**
     * Should the disk be wiped after it's deleted.
     */
    private boolean wipeAfterDelete;

    /**
     * Should disk errors be propagated to the guest?
     */
    private PropagateErrors propagateErrors = PropagateErrors.Off;


    public Disk() {
    }

    public Disk(Guid id,
            int internalDriveMapping,
            DiskType diskType,
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors) {
        super();
        this.id = id;
        this.internalDriveMapping = internalDriveMapping;
        this.diskType = diskType;
        this.diskInterface = diskInterface;
        this.wipeAfterDelete = wipeAfterDelete;
        this.propagateErrors = propagateErrors;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return null;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public int getInternalDriveMapping() {
        return internalDriveMapping;
    }

    public void setInternalDriveMapping(int internalDriveMapping) {
        this.internalDriveMapping = internalDriveMapping;
    }

    @NotNull(message = "VALIDATION.DISK_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    public DiskType getDiskType() {
        return diskType;
    }

    public void setDiskType(DiskType diskType) {
        this.diskType = diskType;
    }

    @NotNull(message = "VALIDATION.DISK_INTERFACE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    public DiskInterface getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(DiskInterface diskInterface) {
        this.diskInterface = diskInterface;
    }

    public boolean isWipeAfterDelete() {
        return wipeAfterDelete;
    }

    public void setWipeAfterDelete(boolean wipeAfterDelete) {
        this.wipeAfterDelete = wipeAfterDelete;
    }

    public PropagateErrors getPropagateErrors() {
        return propagateErrors;
    }

    public void setPropagateErrors(PropagateErrors propagateErrors) {
        this.propagateErrors = propagateErrors;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((diskInterface == null) ? 0 : diskInterface.hashCode());
        result = prime * result + ((diskType == null) ? 0 : diskType.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + internalDriveMapping;
        result = prime * result + ((propagateErrors == null) ? 0 : propagateErrors.hashCode());
        result = prime * result + (wipeAfterDelete ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Disk)) {
            return false;
        }
        Disk other = (Disk) obj;
        if (diskInterface != other.diskInterface) {
            return false;
        }
        if (diskType != other.diskType) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (internalDriveMapping != other.internalDriveMapping) {
            return false;
        }
        if (propagateErrors != other.propagateErrors) {
            return false;
        }
        if (wipeAfterDelete != other.wipeAfterDelete) {
            return false;
        }
        return true;
    }
}
