package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class NumaUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {

    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        // Tune Mode:
        vm.setNumaTuneMode(model.getNumaTuneMode().getSelectedItem());
        // Virtual nodes:
        Integer nodeCount = model.getNumaNodeCount().getEntity();
        // clear NUMA nodes
        if (nodeCount == null || nodeCount == 0) {
            vm.setvNumaNodeList(null);
        } else {
            List<VmNumaNode> nodeList = null;
            if (model.getVmNumaNodes() != null && nodeCount == model.getVmNumaNodes().size()) {
                nodeList = model.getVmNumaNodes();
            } else {
                nodeList = new ArrayList<>(nodeCount);
                for (int i = 0; i < nodeCount; i++) {
                    VmNumaNode newNode = new VmNumaNode();
                    newNode.setIndex(i);
                    nodeList.add(newNode);
                }
            }
            Integer cpuCount = 0;
            for (int i = 0; i < nodeList.size(); i++) {
                VmNumaNode vmNumaNode = nodeList.get(i);
                updateMemory(vmNumaNode, model.getMemSize().getEntity() / nodeCount);
                cpuCount =
                        updateCpus(vmNumaNode,
                                Integer.parseInt(model.getTotalCPUCores().getEntity()) / nodeCount,
                                cpuCount);
            }
            vm.setvNumaNodeList(nodeList);
        }
    }

    private void updateMemory(VmNumaNode vmNumaNode, int memSize) {
        vmNumaNode.setMemTotal(memSize);
    }

    private Integer updateCpus(VmNumaNode vmNumaNode, int coresPerNode, Integer cpuCount) {
        List<Integer> coreList = new ArrayList<>();
        for (int j = 0; j < coresPerNode; j++, cpuCount++) {
            coreList.add(cpuCount);
        }
        vmNumaNode.setCpuIds(coreList);
        return cpuCount;
    }
}
