package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ActionGroupsToRoleParameter")
public class ActionGroupsToRoleParameter extends RolesParameterBase {
    private static final long serialVersionUID = 9211211215875599254L;
    @XmlElement(name = "ActionGroupsJport")
    private ArrayList<ActionGroup> actionGroups;

    public ActionGroupsToRoleParameter() {
    }

    public ActionGroupsToRoleParameter(Guid roleId, ArrayList<ActionGroup> groups) {
        super(roleId);
        this.actionGroups = groups;
    }

    public void setActionGroups(ArrayList<ActionGroup> groups) {
        this.actionGroups = groups;
    }

    public ArrayList<ActionGroup> getActionGroups() {
        return actionGroups;
    }

}
