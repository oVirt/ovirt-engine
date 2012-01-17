package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "roles_relations")
public class roles_relations implements Serializable {
    private static final long serialVersionUID = -8518717766990373742L;

    public roles_relations() {
    }

    public roles_relations(Guid role_container_id, Guid role_id) {
        this.role_container_idField = role_container_id;
        this.role_idField = role_id;
    }

    @XmlElement(name = "role_container_id")
    private Guid role_container_idField = new Guid();

    public Guid getrole_container_id() {
        return this.role_container_idField;
    }

    public void setrole_container_id(Guid value) {
        this.role_container_idField = value;
    }

    @XmlElement(name = "role_id")
    private Guid role_idField = new Guid();

    public Guid getrole_id() {
        return this.role_idField;
    }

    public void setrole_id(Guid value) {
        this.role_idField = value;
    }

}
