package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;

public class RoleWithActionGroupsParameters extends RolesOperationsParameters {
    private static final long serialVersionUID = -4839748040989851074L;
    private ArrayList<ActionGroup> actionGroups;

    public ArrayList<ActionGroup> getActionGroups() {
        return actionGroups == null ? new ArrayList<ActionGroup>() : actionGroups;
    }

    public void setActionGroups(ArrayList<ActionGroup> value) {
        actionGroups = value;
    }

    public RoleWithActionGroupsParameters(Role role, ArrayList<ActionGroup> actions) {
        super(role);
        this.setActionGroups(actions);
    }

    public RoleWithActionGroupsParameters() {
    }
}
