package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RolesParameterBase")
public class RolesParameterBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -1569030140723911754L;

    public RolesParameterBase(Guid roleId) {
        setRoleId(roleId);
    }

    @NotNull
    @XmlElement(name = "RoleId")
    private Guid privateRoleId = new Guid();

    public Guid getRoleId() {
        return privateRoleId;
    }

    public void setRoleId(Guid value) {
        privateRoleId = value;
    }

    public RolesParameterBase() {
    }
}
