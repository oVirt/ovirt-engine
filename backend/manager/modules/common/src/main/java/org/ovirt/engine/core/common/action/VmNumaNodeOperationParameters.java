package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class VmNumaNodeOperationParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -1955959985341097257L;

    private List<VmNumaNode> vmNumaNodeList = new ArrayList<>();
    private VM vm;

    public VmNumaNodeOperationParameters() {
    }

    public VmNumaNodeOperationParameters(Guid vmId, VmNumaNode vmNumaNode) {
        this(vmId, Arrays.asList(vmNumaNode));
    }

    public VmNumaNodeOperationParameters(Guid vmId, List<VmNumaNode> vmNumaNodes) {
        super(vmId);
        if (vmNumaNodes != null){
            vmNumaNodeList = vmNumaNodes;
        }
        vm = null;
    }

    public VmNumaNodeOperationParameters(VM vm, List<VmNumaNode> vmNumaNodes) {
        super(null);
        vmNumaNodeList = vmNumaNodes;
        this.vm = vm;
    }

    public List<VmNumaNode> getVmNumaNodeList() {
        return vmNumaNodeList;
    }


    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }
}
