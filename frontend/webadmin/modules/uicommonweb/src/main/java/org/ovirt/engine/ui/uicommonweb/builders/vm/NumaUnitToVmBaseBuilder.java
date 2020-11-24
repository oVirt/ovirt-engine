package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.NumaUtils;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class NumaUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {

    private static final String HUGEPAGES_PROPERTY = "hugepages"; //$NON-NLS-1$

    @Override
    protected void build(UnitVmModel model, VmBase vm) {
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

        NumaUtils.setNumaListConfiguration(nodeList,
                model.getMemSize().getEntity(),
                getHugePageSize(model),
                Integer.parseInt(model.getTotalCPUCores().getEntity()));
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
