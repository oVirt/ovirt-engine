package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.NumaNode;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;

public class NumaUtils {

    public static void setNumaListConfiguration(List<VmNumaNode> nodeList, long memTotal, Optional<Integer> hugepages,
            int cpuCount) {
        // Sorting is needed, otherwise the list will be ordered by nodeId,
        // as it was returned by DB. It can assign wrong CPU IDs to nodes.
        nodeList.sort(Comparator.comparing(NumaNode::getIndex));
        int nodeCount = nodeList.size();

        // Numa node memory size has to be divisible by the hugepage size
        long memGranularityKB = hugepages.orElse(1024);
        long memGranularityMB = MathUtils.leastCommonMultiple(memGranularityKB, 1024) / 1024;

        long memBlocks = memTotal / memGranularityMB;
        long memBlocksPerNode = memBlocks / nodeCount;
        long remainingBlocks = memBlocks % nodeCount;

        int cpuPerNode = cpuCount / nodeCount;
        int remainingCpus = cpuCount % nodeCount;

        int nextCpuId = 0;
        for (VmNumaNode vmNumaNode : nodeList) {
            // Update Memory
            long nodeBlocks = memBlocksPerNode + (remainingBlocks > 0 ? 1 : 0);
            --remainingBlocks;
            vmNumaNode.setMemTotal(nodeBlocks * memGranularityMB);

            // Update cpus
            int nodeCpus = cpuPerNode + (remainingCpus > 0 ? 1 : 0);
            --remainingCpus;

            List<Integer> coreList = new ArrayList<>(nodeCpus);
            for (int j = 0; j < nodeCpus; j++, nextCpuId++) {
                coreList.add(nextCpuId);
            }
            vmNumaNode.setCpuIds(coreList);
        }
    }

    public static void setNumaListConfiguration(List<VmNumaNode> nodeList, long memTotal, Optional<Integer> hugepages,
            int cpuCount, NumaTuneMode numaTuneMode) {

        setNumaListConfiguration(nodeList, memTotal, hugepages, cpuCount);
        for (VmNumaNode vmNumaNode : nodeList) {
            // Update tune mode
            vmNumaNode.setNumaTuneMode(numaTuneMode);
        }
    }
}
