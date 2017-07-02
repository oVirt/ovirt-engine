package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Role implements Queryable, BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 1487620954798772886L;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;
    private Guid id;
    private boolean readOnly;

    @NotNull(message = "VALIDATION_ROLES_NAME_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.ROLE_NAME_SIZE, message = "VALIDATION_ROLES_NAME_MAX", groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION_ROLES_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    /**
     * MLA addition - distinct admin roles from user roles. Mainly used to prevent user from gaining admin permissions
     */
    private RoleType type;

    private boolean allowsViewingChildren;

    private ApplicationMode appMode;

    public Role() {
        id = Guid.Empty;
        appMode = ApplicationMode.AllModes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                description,
                readOnly,
                name,
                type,
                appMode
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Role)) {
            return false;
        }
        Role other = (Role) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(description, other.description)
                && readOnly == other.readOnly
                && allowsViewingChildren == other.allowsViewingChildren
                && Objects.equals(name, other.name)
                && type == other.type
                && appMode == other.appMode;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isReadonly() {
        return this.readOnly;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public void setReadonly(boolean value) {
        this.readOnly = value;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setType(RoleType type) {
        this.type = type;
    }

    public RoleType getType() {
        return type;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public boolean allowsViewingChildren() {
        return allowsViewingChildren;
    }

    public void setAllowsViewingChildren(boolean allowsViewingChildren) {
        this.allowsViewingChildren = allowsViewingChildren;
    }

    public ApplicationMode getAppMode() {
        return this.appMode;
    }

    public void setAppMode(ApplicationMode mode) {
        this.appMode = mode;
    }
}
