package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * A set which ensures that the unique key of permissions table will not be violated
 */
public class UniquePermissionsSet extends HashSet<PermissionWithUniqueKeyEquals> {

    public void addPermission(Guid adElementId, Guid roleId, Guid objectId, VdcObjectType objectType) {
        add(new PermissionWithUniqueKeyEquals(adElementId, roleId, objectId, objectType));
    }

    /**
     * Converts to instances of permissions class - the rest of the code expects the getClass() to be permissions
     * and not it's child
     */
    public List<permissions> asPermissionList() {
        List<permissions> res = new ArrayList<permissions>();

        for (PermissionWithUniqueKeyEquals permission : this) {
            res.add(permission.asPermission());
        }

        return res;
    }

}

/**
 * Permission which offers the same equals as the unique key constraint on the permissions table
 */
class PermissionWithUniqueKeyEquals extends permissions {

    public PermissionWithUniqueKeyEquals(Guid adElementId, Guid roleId, Guid objectId, VdcObjectType objectType) {
        super(adElementId, roleId, objectId, objectType);
    }

    public permissions asPermission() {
        return new permissions(getad_element_id(), getrole_id(), getObjectId(), getObjectType());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getad_element_id() == null) ? 0 : getad_element_id().hashCode());
        result = prime * result + ((getObjectId() == null) ? 0 : getObjectId().hashCode());
        result = prime * result + ((getrole_id() == null) ? 0 : getrole_id().hashCode());
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
        permissions other = (permissions) obj;
        return (ObjectUtils.objectsEqual(getad_element_id(), other.getad_element_id())
                && ObjectUtils.objectsEqual(getrole_id(), other.getrole_id())
                && ObjectUtils.objectsEqual(getObjectId(), other.getObjectId()));
    }
}
