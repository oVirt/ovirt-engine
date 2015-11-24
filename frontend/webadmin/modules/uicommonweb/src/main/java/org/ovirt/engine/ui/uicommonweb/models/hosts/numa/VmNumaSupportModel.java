package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class VmNumaSupportModel extends NumaSupportModel {

    private final VM vm;

    public VmNumaSupportModel(List<VDS> hosts, VDS host, Model parentModel, VM vm) {
        super(hosts, host, parentModel);
        this.vm = vm;
    }

    @Override
    public void setVmsWithvNumaNodeList(List<VM> vmsWithvNumaNodeList) {
        super.setVmsWithvNumaNodeList(vmsWithvNumaNodeList);
        if (Guid.isNullOrEmpty(vm.getId())) {
            vmsWithvNumaNodeList.add(vm);
        } else {
            VM removeVm = null;
            for (VM iterVm : vmsWithvNumaNodeList) {
                if (iterVm.getId().equals(vm.getId())) {
                    removeVm = iterVm;
                    break;
                }
            }
            vmsWithvNumaNodeList.remove(removeVm);
            vmsWithvNumaNodeList.add(vm);
        }
    }

    @Override
    protected void initVNumaNodes() {
        super.initVNumaNodes();
        lockOtherVmNodes();
    }

    private void lockOtherVmNodes() {
        for (VNodeModel nodeModel : getUnassignedNumaNodes()) {
            if (!nodeModel.getVm().getId().equals(vm.getId())) {
                nodeModel.setLocked(true);
            }
        }

        for (Set<VNodeModel> nodeModelSet : assignedNumaNodes.values()) {
            for (VNodeModel nodeModel : nodeModelSet) {
                if (!nodeModel.getVm().getId().equals(vm.getId())) {
                    nodeModel.setLocked(true);
                }
            }
        }
    }

    public VM getVm() {
        return vm;
    }
}
