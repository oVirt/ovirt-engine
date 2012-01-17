package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MultilevelAdministrationByRoleNameParameters")
public class MultilevelAdministrationByRoleNameParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = 584607807898620094L;

    @XmlElement(name = "RoleName")
    private String privateRoleName;

    public String getRoleName() {
        return privateRoleName;
    }

    private void setRoleName(String value) {
        privateRoleName = value;
    }

    public MultilevelAdministrationByRoleNameParameters(String roleName) {
        setRoleName(roleName);
    }

    public MultilevelAdministrationByRoleNameParameters() {
    }
}
