package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

/**
 * The disk is contains data from the {@link BaseDisk} and the storage specific details for the disk, which are
 * determined by {@link Disk#getDiskStorageType()}.<br>
 * The disk may be attached to a VM or Template, which is indicated by {@link Disk#getVmEntityType()}. If it is null,
 * then the disk is detached (floating).<br>
 * <br>
 * <b>Preferably, use this entity as the base reference wherever you don't need to hold a reference of a specific
 * storage implementation.</b>
 */
public abstract class Disk extends BaseDisk {

    private static final long serialVersionUID = 1380107681294904254L;

    /**
     * The VM Type is indicated by this field, or <code>null</code> if it is detached.
     */
    private VmEntityType vmEntityType;
    private Boolean plugged;
    // TODO comes from image_vm_map
    private Guid vm_guidField = Guid.Empty;

    public Disk() {
    }

    public Disk(Guid id,
            int internalDriveMapping,
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors,
            VmEntityType vmEntityType,
            String diskAlias,
            String diskDescription,
            boolean shareable,
            boolean boot,
            boolean allowSnapshot) {
        super(id,
                internalDriveMapping,
                diskInterface,
                wipeAfterDelete,
                propagateErrors,
                diskAlias,
                diskDescription,
                shareable,
                boot,
                allowSnapshot);
        this.vmEntityType = vmEntityType;
    }

    /**
     * @return The type of underlying storage implementation.
     */
    public abstract DiskStorageType getDiskStorageType();

    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    public Boolean getPlugged() {
        return plugged;
    }

    public void setPlugged(Boolean plugged) {
        this.plugged = plugged;
    }

    public Guid getvm_guid() {
        return this.vm_guidField;
    }

    public void setvm_guid(Guid value) {
        this.vm_guidField = value;
    }

    @Deprecated
    public String getinternal_drive_mapping() {
        return Integer.toString(getInternalDriveMapping());
    }

    @Deprecated
    public void setinternal_drive_mapping(String value) {
        if (value != null) {
            setInternalDriveMapping(Integer.parseInt(value));
        }
    }

    public abstract long getsize();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((plugged == null) ? 0 : plugged.hashCode());
        result = prime * result + ((vmEntityType == null) ? 0 : vmEntityType.hashCode());
        result = prime * result + ((vm_guidField == null) ? 0 : vm_guidField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Disk other = (Disk) obj;
        if (plugged == null) {
            if (other.plugged != null)
                return false;
        } else if (!plugged.equals(other.plugged))
            return false;
        if (vmEntityType != other.vmEntityType)
            return false;
        if (vm_guidField == null) {
            if (other.vm_guidField != null)
                return false;
        } else if (!vm_guidField.equals(other.vm_guidField))
            return false;
        return true;
    }

    /**
     * Enum of the disk's type, which defines which underlying storage details will be contained in the {@link Disk}
     * object instance.
     */
    public enum DiskStorageType {
        IMAGE,
        LUN
    }
}
