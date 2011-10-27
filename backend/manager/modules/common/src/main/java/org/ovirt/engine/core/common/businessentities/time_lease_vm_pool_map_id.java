package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class time_lease_vm_pool_map_id implements Serializable {
    private static final long serialVersionUID = 5057862493956437427L;

    @Type(type = "guid")
    Guid id;

    @Type(type = "guid")
    Guid vmPoolId;

    public time_lease_vm_pool_map_id() {
    }

    public time_lease_vm_pool_map_id(Guid id, Guid vmPoolId) {
        super();
        this.id = id;
        this.vmPoolId = vmPoolId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((vmPoolId == null) ? 0 : vmPoolId.hashCode());
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
        time_lease_vm_pool_map_id other = (time_lease_vm_pool_map_id) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (vmPoolId == null) {
            if (other.vmPoolId != null)
                return false;
        } else if (!vmPoolId.equals(other.vmPoolId))
            return false;
        return true;
    }
}
