package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

@SuppressWarnings("unused")
public class RoleNode {

    public RoleNode(String name, RoleNode[] leafs)
    {
        this.setName(name);
        this.setLeafRoles(new ArrayList<RoleNode>());
        for (RoleNode roleNode : leafs)
        {
            this.getLeafRoles().add(roleNode);
        }
    }

    public RoleNode(String name, String tooltip, RoleNode[] leafs)
    {
        this(name, leafs);
        this.setTooltip(tooltip);
    }

    public RoleNode(String name, RoleNode leaf)
    {
        this.setName(name);
        this.setLeafRoles(new ArrayList<RoleNode>());
        this.getLeafRoles().add(leaf);
    }

    public RoleNode(ActionGroup actionGroup, String tooltip)
    {
        this.setName(actionGroup.toString());
        this.setTooltip(tooltip);
        this.setDesc(EnumTranslator.getInstance().get(actionGroup));
    }

    public RoleNode(String name, String desc)
    {
        this.setName(name);
        this.setDesc(desc);
    }

    private String privateName;

    public String getName()
    {
        return privateName;
    }

    private void setName(String value)
    {
        privateName = value;
    }

    private String privateTooltip;

    public String getTooltip()
    {
        return privateTooltip;
    }

    private void setTooltip(String value)
    {
        privateTooltip = value;
    }

    private String privateDesc;

    public String getDesc()
    {
        return privateDesc;
    }

    private void setDesc(String value)
    {
        privateDesc = value;
    }

    private ArrayList<RoleNode> privateLeafRoles;

    public ArrayList<RoleNode> getLeafRoles()
    {
        return privateLeafRoles;
    }

    private void setLeafRoles(ArrayList<RoleNode> value)
    {
        privateLeafRoles = value;
    }
}
