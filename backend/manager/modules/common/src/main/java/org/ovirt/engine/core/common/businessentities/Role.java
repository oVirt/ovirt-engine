package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Role extends IVdcQueryable implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 1487620954798772886L;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;
    private Guid id;
    private boolean readOnly;

    @NotNull(message = "VALIDATION.ROLES.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.ROLE_NAME_SIZE, message = "VALIDATION.ROLES.NAME.MAX", groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION.ROLES.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (readOnly ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((appMode == null) ? 0 : appMode.getValue());
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
        Role other = (Role) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(description, other.description)
                && readOnly == other.readOnly
                && allowsViewingChildren == other.allowsViewingChildren
                && ObjectUtils.objectsEqual(name, other.name)
                && type == other.type
                && appMode == other.appMode);
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isReadonly() {
        return this.readOnly;
    }

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
