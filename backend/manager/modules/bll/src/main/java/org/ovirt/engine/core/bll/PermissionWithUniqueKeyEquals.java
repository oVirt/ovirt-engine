package org.ovirt.engine.core.bll;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.compat.Guid;

/**
 * Permission which offers the same equals as the unique key constraint on the permissions table
 */
class PermissionWithUniqueKeyEquals extends Permission {

    public PermissionWithUniqueKeyEquals(Guid adElementId, Guid roleId, Guid objectId, VdcObjectType objectType) {
        super(adElementId, roleId, objectId, objectType);
    }

    public Permission asPermission() {
        return new Permission(getAdElementId(), getRoleId(), getObjectId(), getObjectType());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAdElementId() == null) ? 0 : getAdElementId().hashCode());
        result = prime * result + ((getObjectId() == null) ? 0 : getObjectId().hashCode());
        result = prime * result + ((getRoleId() == null) ? 0 : getRoleId().hashCode());
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
        Permission other = (Permission) obj;
        return Objects.equals(getAdElementId(), other.getAdElementId())
                && Objects.equals(getRoleId(), other.getRoleId())
                && Objects.equals(getObjectId(), other.getObjectId());
    }
}
