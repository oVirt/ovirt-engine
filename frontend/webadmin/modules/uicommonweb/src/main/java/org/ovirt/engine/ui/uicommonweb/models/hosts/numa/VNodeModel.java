package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class VNodeModel extends Model {
    private final NumaSupportModel numaSupportModel;
    private final VM vm;
    private final VmNumaNode vmNumaNode;
    private boolean pinned;

    public VNodeModel(NumaSupportModel numaSupportModel, VM vm, VmNumaNode vmNumaNode, boolean pinned) {
        this.numaSupportModel = numaSupportModel;
        this.vm = vm;
        this.vmNumaNode = vmNumaNode;
        this.pinned = pinned;
    }

    public NumaSupportModel getNumaSupportModel() {
        return numaSupportModel;
    }

    public VM getVm() {
        return vm;
    }

    public VmNumaNode getVmNumaNode() {
        return vmNumaNode;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isSplitted() {
        return getVmNumaNode().getVdsNumaNodeList() != null && getVmNumaNode().getVdsNumaNodeList().size() > 1;
    }
}
