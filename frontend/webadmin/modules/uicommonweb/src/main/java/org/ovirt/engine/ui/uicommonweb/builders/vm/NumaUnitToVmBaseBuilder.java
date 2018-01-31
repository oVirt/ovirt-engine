package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.MathUtils;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class NumaUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {

    private static final String HUGEPAGES_PROPERTY = "hugepages"; //$NON-NLS-1$

    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        // Tune Mode:
        vm.setNumaTuneMode(model.getNumaTuneMode().getSelectedItem());
        // Virtual nodes:
        Integer nodeCount = model.getNumaNodeCount().getEntity();
        // clear NUMA nodes
        if (nodeCount == null || nodeCount == 0) {
            vm.setvNumaNodeList(null);
            return;
        }

        List<VmNumaNode> nodeList = model.getVmNumaNodes();
        if (nodeList == null || nodeList.size() != nodeCount) {
            nodeList = new ArrayList<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                VmNumaNode newNode = new VmNumaNode();
                newNode.setIndex(i);
                nodeList.add(newNode);
            }
        }

        long memTotal = model.getMemSize().getEntity();

        // Numa node memory size has to be divisible by the hugepage size
        long memGranularityKB = getHugePageSize(model).orElse(1024);
        long memGranularityMB = MathUtils.leastCommonMultiple(memGranularityKB, 1024) / 1024;

        long memBlocks = memTotal / memGranularityMB;
        long memBlocksPerNode = memBlocks / nodeCount;
        long remainingBlocks = memBlocks % nodeCount;

        int coreCount = Integer.parseInt(model.getTotalCPUCores().getEntity());
        int coresPerNode = coreCount / nodeCount;
        int remainingCores = coreCount % nodeCount;

        int nextCpuId = 0;
        for (VmNumaNode vmNumaNode : nodeList) {
            // Update Memory
            long nodeBlocks = memBlocksPerNode + (remainingBlocks > 0 ? 1 : 0);
            --remainingBlocks;
            vmNumaNode.setMemTotal(nodeBlocks * memGranularityMB);

            // Update cpus
            int nodeCores = coresPerNode + (remainingCores > 0 ? 1 : 0);
            --remainingCores;

            List<Integer> coreList = new ArrayList<>(nodeCores);
            for (int j = 0; j < nodeCores; j++, nextCpuId++) {
                coreList.add(nextCpuId);
            }
            vmNumaNode.setCpuIds(coreList);
        }
        vm.setvNumaNodeList(nodeList);
    }

    private Optional<Integer> getHugePageSize(UnitVmModel model) {
        return model.getCustomPropertySheet().getItems().stream()
                .filter(keyValue -> HUGEPAGES_PROPERTY.equals(keyValue.getKeys().getSelectedItem()))
                .filter(keyValue -> keyValue.getValue().getIsAvailable())
                .map(keyValue -> keyValue.getValue().getEntity())
                .findAny()
                .map(hugePageSizeStr -> {
                    try {
                        return Integer.parseInt(hugePageSizeStr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
    }
}
