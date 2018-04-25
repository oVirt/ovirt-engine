package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
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
    public List<Permission> asPermissionList() {
        List<Permission> res = new ArrayList<>();

        for (PermissionWithUniqueKeyEquals permission : this) {
            res.add(permission.asPermission());
        }

        return res;
    }
}
