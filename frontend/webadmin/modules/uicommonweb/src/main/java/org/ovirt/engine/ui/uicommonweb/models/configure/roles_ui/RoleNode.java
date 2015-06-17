package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

public class RoleNode {

    public RoleNode(String name, RoleNode[] leafs) {
        //The description is what will be shown, if no description passed, use the name as the description.
        this(name, name);
        this.setLeafRoles(new ArrayList<RoleNode>());
        for (RoleNode roleNode : leafs) {
            this.getLeafRoles().add(roleNode);
        }
    }

    public RoleNode(String name, String tooltip, RoleNode[] leafs) {
        this(name, leafs);
        setTooltip(tooltip);
    }

    public RoleNode(String name, RoleNode leaf) {
        this(name, name);
        setLeafRoles(new ArrayList<RoleNode>());
        getLeafRoles().add(leaf);
    }

    public RoleNode(ActionGroup actionGroup, String tooltip) {
        this(actionGroup.toString(), EnumTranslator.getInstance().translate(actionGroup));
        setTooltip(tooltip);
    }

    public RoleNode(String name, String desc) {
        setName(name);
        setDesc(desc);
    }

    private String privateName;

    public String getName() {
        return privateName;
    }

    private void setName(String value) {
        privateName = value;
    }

    private String privateTooltip;

    public String getTooltip() {
        return privateTooltip;
    }

    private void setTooltip(String value) {
        privateTooltip = value;
    }

    private String privateDesc;

    public String getDesc() {
        return privateDesc;
    }

    private void setDesc(String value) {
        privateDesc = value;
    }

    private ArrayList<RoleNode> privateLeafRoles;

    public ArrayList<RoleNode> getLeafRoles() {
        return privateLeafRoles;
    }

    private void setLeafRoles(ArrayList<RoleNode> value) {
        privateLeafRoles = value;
    }
}
