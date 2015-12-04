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

    public Guid getVmId() {
        return this.vmId;
    }

    public void setVmId(Guid value) {
        this.vmId = value;
    }

    private Guid vmPoolId;

    public Guid getVmPoolId() {
        return this.vmPoolId;
    }

    public void setVmPoolId(Guid value) {
        this.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                vmId,
                vmPoolId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmPoolMap)) {
            return false;
        }
        VmPoolMap other = (VmPoolMap) obj;
        return Objects.equals(vmId, other.vmId)
                && Objects.equals(vmPoolId, other.vmPoolId);
    }
}
