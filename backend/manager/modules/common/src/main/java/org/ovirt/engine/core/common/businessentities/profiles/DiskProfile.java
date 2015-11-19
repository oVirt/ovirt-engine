package org.ovirt.engine.core.common.businessentities.profiles;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class DiskProfile extends ProfileBase implements Serializable {
    private static final long serialVersionUID = -7873671967250939737L;

    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    private Guid storageDomainId;

    public DiskProfile() {
        super(ProfileType.DISK);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                storageDomainId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiskProfile)) {
            return false;
        }
        DiskProfile other = (DiskProfile) obj;
        return super.equals(obj)
                && Objects.equals(storageDomainId, other.storageDomainId);
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageDomainId", getStorageDomainId());
    }
}
