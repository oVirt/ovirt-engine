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
        return Objects.hash(getAdElementId(),
                getObjectId(),
                getRoleId());
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
