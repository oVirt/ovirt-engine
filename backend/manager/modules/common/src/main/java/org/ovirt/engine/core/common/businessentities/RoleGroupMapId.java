package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionGroup == null) ? 0 : actionGroup.hashCode());
        result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
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
        RoleGroupMapId other = (RoleGroupMapId) obj;
        if (actionGroup == null) {
            if (other.actionGroup != null)
                return false;
        } else if (!actionGroup.equals(other.actionGroup))
            return false;
        if (roleId == null) {
            if (other.roleId != null)
                return false;
        } else if (!roleId.equals(other.roleId))
            return false;
        return true;
    }
}
