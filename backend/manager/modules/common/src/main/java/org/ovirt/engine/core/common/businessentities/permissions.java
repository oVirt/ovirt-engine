package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "permissions")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class permissions extends IVdcQueryable implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 7249605272394212576L;

    @Column(name = "ad_element_id")
    @Type(type = "guid")
    private Guid adElementId = new Guid();

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "id")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Transient
    private ArrayList<tags> tags;

    @Column(name = "role_id")
    @Type(type = "guid")
    private Guid roleId = new Guid();

    @Column(name = "object_id")
    @Type(type = "guid")
    private Guid objectId;

    @Transient
    private String objectName;

    @Column(name = "object_type_id")
    @Enumerated
    private VdcObjectType objectType;

    @Transient
    private String roleName;

    @Transient
    private String ownerName;

    @Transient
    private RoleType roleType;

    public permissions() {
        this.id = Guid.NewGuid();
    }

    /**
     * @param adElementId
     * @param id
     * @param roleId
     */
    public permissions(Guid adElementId, Guid id, Guid roleId) {
        this.adElementId = adElementId;
        this.id = id;
        this.roleId = roleId;
    }

    public permissions(Guid adElementId, Guid roleId, Guid objectId, VdcObjectType objectType) {
        this.id = Guid.NewGuid();
        this.adElementId = adElementId;
        this.roleId = roleId;
        this.objectId = objectId;
        this.objectType = objectType;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return new java.util.ArrayList<String>();
    }

    public Guid getad_element_id() {
        return this.adElementId;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    public Guid getrole_id() {
        return this.roleId;
    }

    public ArrayList<tags> getTags() {
        return tags;
    }

    public void setad_element_id(Guid value) {
        this.adElementId = value;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    public void setrole_id(Guid value) {
        this.roleId = value;
    }

    public void setTags(ArrayList<tags> value) {
        tags = value;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((adElementId == null) ? 0 : adElementId
                        .hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result
                + ((objectName == null) ? 0 : objectName.hashCode());
        result = prime * result
                + ((objectType == null) ? 0 : objectType.hashCode());
        result = prime * result
                + ((ownerName == null) ? 0 : ownerName.hashCode());
        result = prime * result
                + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result
                + ((roleName == null) ? 0 : roleName.hashCode());
        result = prime * result
                + ((roleType == null) ? 0 : roleType.hashCode());
        result = prime * result
                + ((roleId == null) ? 0 : roleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        permissions other = (permissions) obj;
        if (adElementId == null) {
            if (other.adElementId != null)
                return false;
        } else if (!adElementId.equals(other.adElementId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (objectId == null) {
            if (other.objectId != null)
                return false;
        } else if (!objectId.equals(other.objectId))
            return false;
        if (objectType != other.objectType)
            return false;
        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.equals(other.tags))
            return false;
        if (roleId == null) {
            if (other.roleId != null)
                return false;
        } else if (!roleId.equals(other.roleId))
            return false;
        return true;
    }

}
