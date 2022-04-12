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
            int cpuCount, int threadsPerCore, boolean exclusive) {
        // Sorting is needed, otherwise the list will be ordered by nodeId,
        // as it was returned by DB. It can assign wrong CPU IDs to nodes.
        nodeList.sort(Comparator.comparing(NumaNode::getIndex));
        int nodeCount = nodeList.size();

        // Numa node memory size has to be divisible by the hugepage size
        long memGranularityKB = hugepages.orElse(1024);
        long memGranularityMB = MathUtils.leastCommonMultiple(memGranularityKB, 1024) / 1024;

        long memBlocks = memTotal / memGranularityMB;
        long memBlocksPerCpu = memBlocks / cpuCount;
        boolean hasRemainderMemBlocks = memBlocks % cpuCount != 0;
        long memBlocksPerNode = memBlocks / nodeCount;
        long remainingBlocks = memBlocks % nodeCount;

        int nextCpuId = 0;
        for (VmNumaNode vmNumaNode : nodeList) {
            // Update cpus
            int cpuPerNode = (cpuCount - nextCpuId) / nodeCount--;
            int threadCpuReminder = cpuPerNode % threadsPerCore;
            int nodeCpus = cpuPerNode;
            if (nodeCount != 0 && threadCpuReminder != 0) {
                nodeCpus += threadsPerCore - threadCpuReminder;
            }

            // Update Memory
            if (hasRemainderMemBlocks || exclusive) {
                // splitting memory to the nodes equally
                long nodeBlocks = memBlocksPerNode + (remainingBlocks > 0 ? 1 : 0);
                --remainingBlocks;
                vmNumaNode.setMemTotal(nodeBlocks * memGranularityMB);
            } else {
                // splitting memory based on the CPU amount
                vmNumaNode.setMemTotal(memBlocksPerCpu * nodeCpus * memGranularityMB);
            }

            List<Integer> coreList = new ArrayList<>(nodeCpus);
            for (int j = 0; j < nodeCpus; j++, nextCpuId++) {
                coreList.add(nextCpuId);
            }
            vmNumaNode.setCpuIds(coreList);
        }
    }

    public static void setNumaListConfiguration(List<VmNumaNode> nodeList, long memTotal, Optional<Integer> hugepages,
            int cpuCount, NumaTuneMode numaTuneMode, int threadsPerCore, boolean exclusive) {

        setNumaListConfiguration(nodeList, memTotal, hugepages, cpuCount, threadsPerCore, exclusive);
        for (VmNumaNode vmNumaNode : nodeList) {
            // Update tune mode
            vmNumaNode.setNumaTuneMode(numaTuneMode);
        }
    }
}
