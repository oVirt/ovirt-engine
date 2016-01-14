package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class Permission implements IVdcQueryable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 7249605272394212576L;

    private Guid adElementId;
    private Guid id;
    private Guid roleId;
    private Guid objectId;
    private String objectName;
    private VdcObjectType objectType;
    private String roleName;
    private String ownerName;
    private RoleType roleType;
    private String authz;
    private String namespace;
    /** timestamp taken when that permission was created, in seconds from EPOCH **/
    private long creationDate;

    public Permission() {
        this (Guid.Empty, Guid.Empty, null, null);
    }

    public Permission(Guid adElementId, Guid roleId, Guid objectId, VdcObjectType objectType) {
        this.id = Guid.newGuid();
        this.adElementId = adElementId;
        this.roleId = roleId;
        this.objectId = objectId;
        this.objectType = objectType;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public Guid getAdElementId() {
        return adElementId;
    }

    @Override
    public Guid getId() {
        return id;
    }

    public Guid getRoleId() {
        return roleId;
    }

    public void setAdElementId(Guid adElementId) {
        this.adElementId = adElementId;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public void setRoleId(Guid roleId) {
        this.roleId = roleId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public VdcObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(VdcObjectType objectType) {
        this.objectType = objectType;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getAuthz() {
        return authz;
    }

    public void setAuthz(String authz) {
        this.authz = authz;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     *
     * @return the timestamp taken when that permission was created, in seconds from EPOCH
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * set the creation date of this enity
     * @param creationDate must be the seconds from EPOCH
     */
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((adElementId == null) ? 0 : adElementId.hashCode());
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
        result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
        result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
        result = prime * result + ((roleType == null) ? 0 : roleType.hashCode());
        result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
        result = prime * result + ((authz == null) ? 0 : authz.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + (int) creationDate;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Permission)) {
            return false;
        }
        Permission other = (Permission) obj;
        return creationDate != ((Permission) obj).creationDate
                && Objects.equals(id, other.id)
                && Objects.equals(adElementId, other.adElementId)
                && Objects.equals(objectId, other.objectId)
                && objectType == other.objectType
                && Objects.equals(roleId, other.roleId);
    }



}
