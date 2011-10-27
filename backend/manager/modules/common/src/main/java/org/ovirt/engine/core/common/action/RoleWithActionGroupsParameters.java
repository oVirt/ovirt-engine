package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RoleWithActionGroupsParameters")
public class RoleWithActionGroupsParameters extends RolesOperationsParameters {
    private static final long serialVersionUID = -4839748040989851074L;
    @XmlElement(name = "ActionGroupsJport")
    private ArrayList<ActionGroup> actionGroups;

    public ArrayList<ActionGroup> getActionGroups() {
        return actionGroups == null ? new ArrayList<ActionGroup>() : actionGroups;
    }

    public void setActionGroups(ArrayList<ActionGroup> value) {
        actionGroups = value;
    }

    public RoleWithActionGroupsParameters(roles role, ArrayList<ActionGroup> actions) {
        super(role);
        this.setActionGroups(actions);
    }

    public RoleWithActionGroupsParameters() {
    }
}
