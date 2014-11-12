package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmStatic;

public class HotSetAmountOfMemoryParameters extends VmManagementParametersBase  {

    private PlugAction plugAction;
    private int numaNode;

    public HotSetAmountOfMemoryParameters() {
    }

    public HotSetAmountOfMemoryParameters(VmStatic vmStatic, PlugAction plugAction, int numaNode) {
        super(vmStatic);
        this.plugAction = plugAction;
        this.numaNode = numaNode;
    }

    public PlugAction getPlugAction() {
        return plugAction;
    }

    public int getNumaNode() {
        return numaNode;
    }

    public void setNumaNode(int numaNode) {
        this.numaNode = numaNode;
    }
}
