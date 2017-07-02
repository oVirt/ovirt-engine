package org.ovirt.engine.core.common.businessentities.profiles;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public abstract class ProfileBase implements Queryable, BusinessEntity<Guid>, Nameable {
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
        return Objects.hash(
                description,
                id,
                name,
                profileType,
                qosId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProfileBase)) {
            return false;
        }
        ProfileBase other = (ProfileBase) obj;
        return Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && profileType == other.profileType
                && Objects.equals(qosId, other.qosId);
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("id", getId())
                .append("description", getDescription())
                .append("profileType", getProfileType())
                .append("qosId", getQosId());

    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
