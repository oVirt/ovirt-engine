package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RolesActionMapParameters")
public class RolesActionMapParameters extends RolesParameterBase {
    private static final long serialVersionUID = 4800306282543787332L;
    @XmlElement(name = "RoleActionMap")
    private roles_actions privateRoleActionMap;

    public roles_actions getRoleActionMap() {
        return privateRoleActionMap;
    }

    private void setRoleActionMap(roles_actions value) {
        privateRoleActionMap = value;
    }

    public RolesActionMapParameters(roles_actions roleActionMap) {
        super(roleActionMap.getrole_id());
        setRoleActionMap(roleActionMap);
    }

    public RolesActionMapParameters() {
    }
}
