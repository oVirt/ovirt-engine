package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "roles_groups")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class RoleGroupMap implements Serializable {
    private static final long serialVersionUID = -230507879656573010L;

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "actionGroup", column = @Column(name = "action_group_id")),
        @AttributeOverride(name = "roleId", column = @Column(name = "role_id")) })
    private RoleGroupMapId id = new RoleGroupMapId();

    public RoleGroupMap() {
    };

    public RoleGroupMap(ActionGroup actionGroup, Guid roleId) {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        RoleGroupMap other = (RoleGroupMap) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
