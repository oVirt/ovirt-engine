package org.ovirt.engine.core.common.businessentities.profiles;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class CpuProfile extends ProfileBase implements Serializable {
    private static final long serialVersionUID = -7873671967250011737L;

    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    private Guid clusterId;

    public CpuProfile() {
        super(ProfileType.CPU);
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
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
        CpuProfile other = (CpuProfile) obj;
        if (clusterId == null) {
            if (other.clusterId != null)
                return false;
        } else if (!clusterId.equals(other.clusterId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + ", cluster id=" + getClusterId() + "}";
    }

}
