package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class GetPermissionsForObjectParameters extends QueryParametersBase {
    private static final long serialVersionUID = 4719409151543629037L;

    private Guid objectId;

    /**
     * True to get only the direct permission of an object. False - get implicit permissions on an object example -
     * implicit VM permissions will return the VM, its Cluster, its Datacenter and System permissions.
     */
    private boolean directOnly;

    private boolean allUsersWithPermission;

    private VdcObjectType vdcObjectType;

    public GetPermissionsForObjectParameters() {
        directOnly = true;
    }

    public GetPermissionsForObjectParameters(Guid objectId) {
        this();
        this.objectId = objectId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid objectId) {
        this.objectId = objectId;
    }

    public void setDirectOnly(boolean directOnly) {
        this.directOnly = directOnly;
    }

    public boolean getDirectOnly() {
        return directOnly;
    }

    public void setVdcObjectType(VdcObjectType vdcObjectType) {
        this.vdcObjectType = vdcObjectType;
    }

    public VdcObjectType getVdcObjectType() {
        return vdcObjectType;
    }

    public boolean getAllUsersWithPermission() {
        return allUsersWithPermission;
    }

    public void setAllUsersWithPermission(boolean getAllUserPermissions) {
        this.allUsersWithPermission = getAllUserPermissions;
    }

}
