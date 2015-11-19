package org.ovirt.engine.core.common.businessentities.profiles;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
        return Objects.hash(
                super.hashCode(),
                clusterId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CpuProfile)) {
            return false;
        }
        CpuProfile other = (CpuProfile) obj;
        return super.equals(obj)
                && Objects.equals(clusterId, other.clusterId);
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("clusterId", getClusterId());
    }
}
