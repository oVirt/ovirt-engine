package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.validation.annotation.ValidName;
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
     * The alias name of the disk.
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, groups = { CreateEntity.class })
    @ValidName(message = "VALIDATION.DISK.ALIAS.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String diskAlias;

    /**
     * The description of the disk.
     */
    private String diskDescription;

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
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors) {
        super();
        this.id = id;
        this.internalDriveMapping = internalDriveMapping;
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

    public String getDiskDescription() {
        return diskDescription;
    }

    public void setDiskDescription(String diskDescription) {
        this.diskDescription = diskDescription;
    }

    public String getDiskAlias() {
        return diskAlias;
    }

    public void setDiskAlias(String diskAlias) {
        this.diskAlias = diskAlias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((diskDescription == null) ? 0 : diskDescription.hashCode());
        result = prime * result + ((diskInterface == null) ? 0 : diskInterface.hashCode());
        result = prime * result + ((diskAlias == null) ? 0 : diskAlias.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + internalDriveMapping;
        result = prime * result + ((propagateErrors == null) ? 0 : propagateErrors.hashCode());
        result = prime * result + (wipeAfterDelete ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Disk other = (Disk) obj;
        if (diskDescription == null) {
            if (other.diskDescription != null)
                return false;
        } else if (!diskDescription.equals(other.diskDescription))
            return false;
        if (diskInterface != other.diskInterface)
            return false;
        if (diskAlias == null) {
            if (other.diskAlias != null)
                return false;
        } else if (!diskAlias.equals(other.diskAlias))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (internalDriveMapping != other.internalDriveMapping)
            return false;
        if (propagateErrors != other.propagateErrors)
            return false;
        if (wipeAfterDelete != other.wipeAfterDelete)
            return false;
        return true;
    }
}
