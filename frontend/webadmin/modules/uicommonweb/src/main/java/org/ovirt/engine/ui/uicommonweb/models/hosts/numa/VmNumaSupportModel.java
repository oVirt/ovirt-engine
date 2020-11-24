package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class VmNumaSupportModel extends NumaSupportModel {

    private final VM vm;
    private final VDS host;

    public VmNumaSupportModel(List<VDS> hosts, VDS host, Model parentModel, VM vm) {
        super(hosts, host, parentModel);
        this.vm = vm;
        this.host = host;
    }

    @Override
    public void setVmsWithvNumaNodeList(List<VM> vmsWithvNumaNodeList) {
        super.setVmsWithvNumaNodeList(vmsWithvNumaNodeList);

        // Fix displayed VMs list according to model configuration
        if (getParentModel() instanceof UnitVmModel && host.isNumaSupport()) {
            UnitVmModel model = (UnitVmModel) getParentModel();
            if (model.getNumaNodeCount().getEntity() > 0 && !vmsWithvNumaNodeList.contains(vm)) {
                vmsWithvNumaNodeList.add(vm);
            } else if (model.getNumaNodeCount().getEntity() == 0) {
                vmsWithvNumaNodeList.remove(vm);
            }
        }

        if (Guid.isNullOrEmpty(vm.getId())) {
            if (getParentModel() instanceof UnitVmModel) {
                UnitVmModel model = (UnitVmModel) getParentModel();
                if (model.getVmNumaNodes() != null) {
                    // maintains NUMA pinning settings in UI prior to save
                    // for new VMs
                    this.getVm().setvNumaNodeList(model.getVmNumaNodes());
                }
            }
        } else {
            for (VM vmFromDb : vmsWithvNumaNodeList) {
                if (vmFromDb.getId().equals(vm.getId())) {
                    // maintains NUMA pinning settings in UI prior to save
                    // for existing VMs
                    vmFromDb.setvNumaNodeList(vm.getvNumaNodeList());
                    break;
                }
            }
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

    @Override
    protected VNodeModel createVNodeModel(VM vm, VmNumaNode vmNumaNode) {
        return new VNodeModel(vm, vmNumaNode, getVm().equals(vm));
    }
}
