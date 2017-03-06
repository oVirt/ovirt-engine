package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmStatic;

public class HotSetAmountOfMemoryParameters extends VmManagementParametersBase  {

    private PlugAction plugAction;
    private int numaNode;
    private int memoryDeviceSizeMb;

    public HotSetAmountOfMemoryParameters() {
    }

    public HotSetAmountOfMemoryParameters(VmStatic vmStatic,
            PlugAction plugAction,
            int numaNode,
            int memoryDeviceSizeMb) {
        super(vmStatic);
        this.plugAction = plugAction;
        this.numaNode = numaNode;
        this.memoryDeviceSizeMb = memoryDeviceSizeMb;
    }

    public int getMemoryDeviceSizeMb() {
        return memoryDeviceSizeMb;
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
