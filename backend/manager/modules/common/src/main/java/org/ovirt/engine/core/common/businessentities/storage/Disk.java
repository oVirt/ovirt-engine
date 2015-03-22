package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.utils.ObjectUtils;

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
    private boolean ovfStore;

    /**
     * Plugged and readOnly are of type Boolean (as opposed to boolean) since they are optional.
     * In case the disk is not in a vm context, null will ensure they are invisible.
     */
    private Boolean plugged;
    private Boolean readOnly;
    private String logicalName;

    /**
     * @return Whether taking snapshots of this disk is allowed
     */
    public abstract boolean isAllowSnapshot();

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

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public abstract long getSize();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((plugged == null) ? 0 : plugged.hashCode());
        result = prime * result + ((readOnly == null) ? 0 : readOnly.hashCode());
        result = prime * result + ((vmNames == null) ? 0 : vmNames.hashCode());
        result = prime * result + ((vmEntityType == null) ? 0 : vmEntityType.hashCode());
        result = prime * result + numberOfVms;
        result = prime * result + ((logicalName == null) ? 0 : logicalName.hashCode());
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
                && ObjectUtils.objectsEqual(readOnly, other.readOnly)
                && ObjectUtils.objectsEqual(vmNames, other.vmNames)
                && ObjectUtils.objectsEqual(logicalName, other.logicalName)
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

    @JsonIgnore
    public boolean isDiskSnapshot() {
        return false;
    }

    public boolean isOvfStore() {
        return ovfStore;
    }

    public void setOvfStore(boolean ovfStore) {
        this.ovfStore = ovfStore;
    }
}
