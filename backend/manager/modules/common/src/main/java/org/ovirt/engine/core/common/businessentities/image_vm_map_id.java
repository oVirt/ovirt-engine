package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class image_vm_map_id implements Serializable {
    private static final long serialVersionUID = 2283977222487071390L;

    private Guid imageId;
    private Guid vmId;

    public image_vm_map_id() {
    }

    public image_vm_map_id(Guid imageId, Guid vmId) {
        this.imageId = imageId;
        this.vmId = vmId;
    }

    /**
     * @return the imageId
     */
    public Guid getImageId() {
        return imageId;
    }

    /**
     * @param imageId the imageId to set
     */
    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    /**
     * @return the vmId
     */
    public Guid getVmId() {
        return vmId;
    }

    /**
     * @param vmId the vmId to set
     */
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        image_vm_map_id other = (image_vm_map_id) obj;
        if (imageId == null) {
            if (other.imageId != null) {
                return false;
            }
        } else if (!imageId.equals(other.imageId)) {
            return false;
        }
        if (vmId == null) {
            if (other.vmId != null) {
                return false;
            }
        } else if (!vmId.equals(other.vmId)) {
            return false;
        }
        return true;
    }

}
