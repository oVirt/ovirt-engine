package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MultilevelAdministrationByRoleIdParameters")
public class MultilevelAdministrationByRoleIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = -6638689960055937254L;

    @NotNull(message = "VALIDATION.ROLES.ID.NOT_NULL")
    @XmlElement(name = "RoleId")
    private Guid privateRoleId = new Guid();

    public Guid getRoleId() {
        return privateRoleId;
    }

    private void setRoleId(Guid value) {
        privateRoleId = value;
    }

    public MultilevelAdministrationByRoleIdParameters(Guid roleId) {
        setRoleId(roleId);
    }

    public MultilevelAdministrationByRoleIdParameters() {
    }
}
