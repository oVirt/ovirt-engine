package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.action.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "roles_actions")
public class roles_actions implements Serializable {
    private static final long serialVersionUID = 2827087421933303609L;

    public roles_actions() {
    }

    public roles_actions(VdcActionType action_id, Guid role_id) {
        this.action_idField = action_id;
        this.role_idField = role_id;
    }

    @XmlElement(name = "action_id")
    private VdcActionType action_idField = VdcActionType.forValue(0);

    public VdcActionType getaction_id() {
        return this.action_idField;
    }

    public void setaction_id(VdcActionType value) {
        this.action_idField = value;
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
