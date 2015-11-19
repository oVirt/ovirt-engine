package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMapId implements Serializable {
    private static final long serialVersionUID = 2602534834702940699L;

    Integer actionGroup;

    Guid roleId;

    public RoleGroupMapId() {
    }

    public RoleGroupMapId(Guid roleId, ActionGroup actionGroup) {
        this.roleId = roleId;
        this.actionGroup = actionGroup.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                actionGroup,
                roleId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RoleGroupMapId)) {
            return false;
        }
        RoleGroupMapId other = (RoleGroupMapId) obj;
        return Objects.equals(actionGroup, other.actionGroup)
                && Objects.equals(roleId, other.roleId);
    }
}
