package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class Permission implements Queryable, BusinessEntity<Guid> {
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
        return Objects.hash(id,
                adElementId,
                objectId,
                objectName,
                objectType,
                ownerName,
                roleName,
                roleType,
                roleId,
                authz,
                namespace,
                creationDate);
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
