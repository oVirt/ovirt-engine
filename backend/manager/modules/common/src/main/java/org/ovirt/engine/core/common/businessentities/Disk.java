package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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
    private int numberOfVms;
    private ArrayList<String> vmNames;
    private Boolean plugged;

    public Disk() {
    }

    public Disk(Guid id,
            DiskInterface diskInterface,
            boolean wipeAfterDelete,
            PropagateErrors propagateErrors,
            VmEntityType vmEntityType,
            int numberOfVms,
            ArrayList<String> vmNames,
            String diskAlias,
            String diskDescription,
            boolean shareable,
            boolean boot) {
        super(id,
                diskInterface,
                wipeAfterDelete,
                propagateErrors,
                diskAlias,
                diskDescription,
                shareable,
                boot);
        this.vmEntityType = vmEntityType;
        this.numberOfVms = numberOfVms;
        this.vmNames = vmNames;
    }

    /**
     * @return Whether taking snapshots of this disk is allowed
     */
    public abstract boolean isAllowSnapshot();

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

    public abstract long getSize();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + numberOfVms;
        result = prime * result + ((plugged == null) ? 0 : plugged.hashCode());
        result = prime * result + ((vmEntityType == null) ? 0 : vmEntityType.hashCode());
        result = prime * result + ((vmNames == null) ? 0 : vmNames.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Disk other = (Disk) obj;
        return (ObjectUtils.objectsEqual(plugged, other.plugged)
                && ObjectUtils.objectsEqual(vmNames, other.vmNames)
                && vmEntityType == other.vmEntityType
                && numberOfVms == other.numberOfVms);
    }

    public int getNumberOfVms() {
        return numberOfVms;
    }

    public void setNumberOfVms(int numberOfVms) {
        this.numberOfVms = numberOfVms;
    }

    public ArrayList<String> getVmNames() {
        return vmNames;
    }

    public void setVmNames(ArrayList<String> vmNames) {
        this.vmNames = vmNames;
    }

    /**
     * Enum of the disk's type, which defines which underlying storage details will be contained in the {@link Disk}
     * object instance.
     */
    public enum DiskStorageType implements Identifiable {
        // FIXME add ids and remove the ordinal impl of getValue
        IMAGE,
        LUN;

        @Override
        public int getValue() {
            return this.ordinal();
        }

        public static DiskStorageType forValue(int value) {
            return values()[value];
        }
    }
}
