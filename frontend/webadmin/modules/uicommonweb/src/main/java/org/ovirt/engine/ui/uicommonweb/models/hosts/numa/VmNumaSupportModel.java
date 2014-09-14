package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import java.util.List;

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
        super.setVmsWithvNumaNodeList(vmsWithvNumaNodeList);
    }

    @Override
    protected void initVNumaNodes() {
        super.initVNumaNodes();
        lockOtherVmNodes();
    }

    private void lockOtherVmNodes() {
        for (VNodeModel nodeModel : getUnassignedVNodeModelList()) {
            if (!nodeModel.getVm().getId().equals(vm.getId())) {
                nodeModel.setLocked(true);
            }
        }

        for (List<VNodeModel> list : p2vNumaNodesMap.values()) {
            for (VNodeModel nodeModel : list) {
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
