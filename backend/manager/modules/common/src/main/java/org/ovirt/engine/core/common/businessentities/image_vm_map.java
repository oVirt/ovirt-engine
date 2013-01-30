package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class image_vm_map implements BusinessEntity<image_vm_map_id> {
    private static final long serialVersionUID = -6528043171116600954L;

    private image_vm_map_id id = new image_vm_map_id();
    private Boolean active;

    public image_vm_map() {
    }

    public image_vm_map(Boolean active, Guid image_id, Guid vm_id) {
        this.active = active;
        getId().setImageId(image_id);
        getId().setVmId(vm_id);
    }

    public Boolean getactive() {
        return this.active;
    }

    public void setactive(Boolean value) {
        this.active = value;
    }

    public Guid getimage_id() {
        return getId().getImageId();
    }

    public void setimage_id(Guid value) {
        getId().setImageId(value);
    }

    public Guid getvm_id() {
        return getId().getVmId();
    }

    public void setvm_id(Guid value) {
        getId().setVmId(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((active == null) ? 0 : active.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        image_vm_map other = (image_vm_map) obj;
        if (active == null) {
            if (other.active != null) {
                return false;
            }
        } else if (!active.equals(other.active)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * @return the id
     */
    @Override
    public image_vm_map_id getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(image_vm_map_id id) {
        this.id = id;
    }
}
