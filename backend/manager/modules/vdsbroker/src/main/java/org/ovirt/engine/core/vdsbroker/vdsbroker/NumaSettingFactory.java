package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.utils.NumaUtils;

public class NumaSettingFactory {

    public static List<Map<String, Object>> buildVmNumaNodeSetting(List<VmNumaNode> vmNumaNodes) {
        List<Map<String, Object>> createVmNumaNodes = new ArrayList<>();
        for (VmNumaNode node : vmNumaNodes) {
            Map<String, Object> createVmNumaNode = new HashMap<>();
            createVmNumaNode.put(VdsProperties.NUMA_NODE_CPU_LIST, NumaUtils.buildStringFromListForNuma(node.getCpuIds()));
            createVmNumaNode.put(VdsProperties.VM_NUMA_NODE_MEM, String.valueOf(node.getMemTotal()));
            createVmNumaNode.put(VdsProperties.NUMA_NODE_INDEX, node.getIndex());
            createVmNumaNodes.add(createVmNumaNode);
        }
        return createVmNumaNodes;
    }

    public static Map<String, Object> buildCpuPinningWithNumaSetting(List<VmNumaNode> vmNodes, List<VdsNumaNode> vdsNodes) {
        Map<Integer, List<Integer>> vdsNumaNodeCpus = new HashMap<>();
        Map<String, Object> cpuPinDict = new HashMap<>();
        for (VdsNumaNode node : vdsNodes) {
            vdsNumaNodeCpus.put(node.getIndex(), node.getCpuIds());
        }
        for (VmNumaNode node : vmNodes) {
            List<Integer> pinnedNodeIndexes = node.getVdsNumaNodeList();
            if (!pinnedNodeIndexes.isEmpty()) {
                Set<Integer> totalPinnedVdsCpus = new LinkedHashSet<>();

                for (Integer pinnedVdsNode : pinnedNodeIndexes) {
                    totalPinnedVdsCpus.addAll(vdsNumaNodeCpus.getOrDefault(pinnedVdsNode, Collections.emptyList()));
                }

                for (Integer vCpu : node.getCpuIds()) {
                    cpuPinDict.put(String.valueOf(vCpu), NumaUtils.buildStringFromListForNuma(totalPinnedVdsCpus));
                }
            }
        }
        return cpuPinDict;
    }

    public static Map<String, Object> buildVmNumatuneSetting(
            NumaTuneMode numaTuneMode,
            List<VmNumaNode> vmNumaNodes) {

        List<Map<String, String>> memNodeList = new ArrayList<>();
        for (VmNumaNode node : vmNumaNodes) {
            if (node.getVdsNumaNodeList().isEmpty()) {
                continue;
            }

            Map<String, String> memNode = new HashMap<>();
            memNode.put(VdsProperties.NUMA_TUNE_VM_NODE_INDEX, String.valueOf(node.getIndex()));
            memNode.put(VdsProperties.NUMA_TUNE_NODESET,
                    NumaUtils.buildStringFromListForNuma(node.getVdsNumaNodeList()));

            memNodeList.add(memNode);
        }

        // If no node is pinned, leave pinning implicit
        if (memNodeList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> createNumaTune = new HashMap<>();
        createNumaTune.put(VdsProperties.NUMA_TUNE_MEMNODES, memNodeList);
        createNumaTune.put(VdsProperties.NUMA_TUNE_MODE, numaTuneMode.getValue());

        return createNumaTune;
    }
}
