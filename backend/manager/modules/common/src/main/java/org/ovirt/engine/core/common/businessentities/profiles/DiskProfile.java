package org.ovirt.engine.core.common.businessentities.profiles;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
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
        DiskProfile other = (DiskProfile) obj;
        if (storageDomainId == null) {
            if (other.storageDomainId != null)
                return false;
        } else if (!storageDomainId.equals(other.storageDomainId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + ", storage domain id=" + getStorageDomainId() + "}";
    }

}
