package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.roles;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RolesOperationsParameters")
public class RolesOperationsParameters extends RolesParameterBase {
    private static final long serialVersionUID = -1785886308686587013L;
    @Valid
    @XmlElement(name = "Role")
    private roles privateRole;

    public roles getRole() {
        return privateRole;
    }

    public void setRole(roles value) {
        privateRole = value;
    }

    public RolesOperationsParameters(roles role) {
        super(role.getId());
        setRole(role);
    }

    public RolesOperationsParameters() {
    }
}
