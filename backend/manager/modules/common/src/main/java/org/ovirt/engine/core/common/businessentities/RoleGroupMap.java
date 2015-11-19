package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMap implements Serializable {
    private static final long serialVersionUID = -230507879656573010L;

    private RoleGroupMapId id;

    public RoleGroupMap() {
        id = new RoleGroupMapId();
    }

    public RoleGroupMap(ActionGroup actionGroup, Guid roleId) {
        this();
        this.id.actionGroup = actionGroup.getId();
        this.id.roleId = roleId;
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.id.actionGroup = actionGroup.getId();
    }

    public ActionGroup getActionGroup() {
        return ActionGroup.forValue(id.actionGroup);
    }

    public void setRoleId(Guid roleId) {
        this.id.roleId = roleId;
    }

    public Guid getRoleId() {
        return id.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RoleGroupMap)) {
            return false;
        }
        RoleGroupMap other = (RoleGroupMap) obj;
        return Objects.equals(id, other.id);
    }
}
