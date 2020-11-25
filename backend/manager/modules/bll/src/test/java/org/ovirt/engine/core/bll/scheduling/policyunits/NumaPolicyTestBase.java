package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class NumaPolicyTestBase {

    protected static VmNumaNode createVmNode(long size, int index, List<Integer> pinnedList) {
        return createVmNode(size, index, pinnedList, null);
    }

    protected static VmNumaNode createVmNode(long size, int index, List<Integer> pinnedList, List<Integer> cpuIds) {
        VmNumaNode node = new VmNumaNode();
        node.setId(Guid.newGuid());
        node.setIndex(index);
        node.setMemTotal(size);
        node.setVdsNumaNodeList(pinnedList);
        node.setCpuIds(cpuIds);
        node.setNumaTuneMode(NumaTuneMode.STRICT);
        return node;
    }

    protected static VDS createHost(int nodeCount, long nodeSize) {
        return createHost(nodeCount, nodeSize, 1);
    }

    protected static VDS createHost(int nodeCount, long nodeSize, int coresPerNode) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setNumaNodeList(new ArrayList<>());
        host.setNumaSupport(nodeCount > 0);

        host.setCpuSockets(Math.max(nodeCount, 1));
        host.setCpuCores(coresPerNode);
        host.setCpuThreads(1);

        for (int i = 0; i < nodeCount; ++i) {
            VdsNumaNode node = new VdsNumaNode();
            node.setId(Guid.newGuid());
            node.setIndex(i);
            node.setMemTotal(nodeSize);

            node.setNumaNodeStatistics(new NumaNodeStatistics());
            node.getNumaNodeStatistics().setMemFree(nodeSize);

            node.setCpuIds(IntStream.range(i * coresPerNode, (i+1) * coresPerNode)
                    .boxed()
                    .collect(Collectors.toList())
            );

            host.getNumaNodeList().add(node);
        }

        return host;
    }
}
