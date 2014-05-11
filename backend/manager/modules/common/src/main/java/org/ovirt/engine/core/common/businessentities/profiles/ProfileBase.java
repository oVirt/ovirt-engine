package org.ovirt.engine.core.common.businessentities.profiles;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public abstract class ProfileBase extends IVdcQueryable implements BusinessEntity<Guid>, Nameable, Serializable {
    private static final long serialVersionUID = 1055016330475623255L;

    @NotNull(groups = { UpdateEntity.class, RemoveEntity.class })
    private Guid id;
    @Size(min = 1, max = BusinessEntitiesDefinitions.PROFILE_NAME_SIZE, groups = { CreateEntity.class,
            UpdateEntity.class })
    @ValidName(message = "VALIDATION_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;
    private Guid qosId;
    private String description;
    private ProfileType profileType;

    @SuppressWarnings("unused")
    private ProfileBase() {
    }

    public ProfileBase(ProfileType profileType) {
        this.setProfileType(profileType);
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    private void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Guid getQosId() {
        return qosId;
    }

    public void setQosId(Guid qosId) {
        this.qosId = qosId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 31;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((profileType == null) ? 0 : profileType.hashCode());
        result = prime * result + ((qosId == null) ? 0 : qosId.hashCode());
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
        ProfileBase other = (ProfileBase) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (profileType != other.profileType)
            return false;
        if (qosId == null) {
            if (other.qosId != null)
                return false;
        } else if (!qosId.equals(other.qosId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append(" {id=")
                .append(getId())
                .append(", description=")
                .append(getDescription())
                .append(", profile type=")
                .append(getProfileType().name())
                .append(", qosId=")
                .append(getQosId());
        return builder.toString();
    }

}
