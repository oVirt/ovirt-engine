package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolMap implements Serializable {
    private static final long serialVersionUID = 5876397644156138863L;

    public VmPoolMap() {
        this (Guid.Empty, null);
    }

    public VmPoolMap(Guid vm_guid, Guid vm_pool_id) {
        this.vmId = vm_guid;
        this.vmPoolId = vm_pool_id;
    }

    private Guid vmId;

    public Guid getvm_guid() {
        return this.vmId;
    }

    public void setvm_guid(Guid value) {
        this.vmId = value;
    }

    private Guid vmPoolId;

    public Guid getvm_pool_id() {
        return this.vmPoolId;
    }

    public void setvm_pool_id(Guid value) {
        this.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vmPoolId == null) ? 0 : vmPoolId.hashCode());
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
        VmPoolMap other = (VmPoolMap) obj;
        return (Objects.equals(vmId, other.vmId)
                && Objects.equals(vmPoolId, other.vmPoolId));
    }
}
