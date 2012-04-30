package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class ActionGroupsToRoleParameter extends RolesParameterBase {
    private static final long serialVersionUID = 9211211215875599254L;
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
