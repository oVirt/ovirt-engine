package org.ovirt.engine.core.bll.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public class NumaTestUtils {

    public static VdsNumaNode createVdsNumaNode(int index) {
        VdsNumaNode vdsNumaNode = new VdsNumaNode();
        vdsNumaNode.setIndex(index);
        vdsNumaNode.setId(Guid.newGuid());
        vdsNumaNode.setMemTotal(2000);
        return vdsNumaNode;

    }

    public static VmNumaNode createVmNumaNodeWithId(int index, Guid guid) {
       final VmNumaNode numaNode = createVmNumaNode(index);
        numaNode.setId(guid);
        return numaNode;
    }

    public static VmNumaNode createVmNumaNodeWithId(int index, List<VdsNumaNode> vdsNumaNodes, Guid guid) {
       final VmNumaNode numaNode = createVmNumaNode(index, vdsNumaNodes);
        numaNode.setId(guid);
        return numaNode;
    }

    public static VmNumaNode createVmNumaNode(int index) {
        VmNumaNode vmNumaNode = new VmNumaNode();
        vmNumaNode.setIndex(index);
        vmNumaNode.setMemTotal(1000);
        return vmNumaNode;
    }

    public static VmNumaNode createVmNumaNode(int index, List<VdsNumaNode> vdsNumaNodes) {
        VmNumaNode numaNode = createVmNumaNode(index);
        final List<Pair<Guid, Pair<Boolean, Integer>>> numaPinning = new ArrayList<>();
        for (VdsNumaNode vdsNumaNode : vdsNumaNodes) {
            numaPinning.add(toWeirdPair(vdsNumaNode));
        }
        numaNode.setVdsNumaNodeList(numaPinning);
        return numaNode;
    }

    public static Pair<Guid, Pair<Boolean, Integer>> toWeirdPair(final VdsNumaNode vdsNumaNode) {
        return new Pair<>(vdsNumaNode.getId(), new Pair<>(true, vdsNumaNode.getIndex()));
    }

    public static void mockVdsNumaNodeDao(final VdsNumaNodeDao vdsNumaNodeDao, VdsNumaNode... vdsNumaNodes) {
        mockVdsNumaNodeDao(vdsNumaNodeDao, Arrays.asList(vdsNumaNodes));
    }

    public static void mockVdsNumaNodeDao(final VdsNumaNodeDao vdsNumaNodeDao, List<VdsNumaNode> vdsNumaNodes) {
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class)))
                .thenReturn(vdsNumaNodes);

    }

    public static void mockVmNumaNodeDao(final VmNumaNodeDao vmNumaNodeDao, List<VmNumaNode> vmNumaNodes) {
        when(vmNumaNodeDao.getAllVmNumaNodeByVmId(any(Guid.class)))
                .thenReturn(vmNumaNodes);
    }

    public static void mockVmNumaNodeDao(final VmNumaNodeDao vmNumaNodeDao, VmNumaNode... vmNumaNodes) {
        mockVmNumaNodeDao(vmNumaNodeDao, Arrays.asList(vmNumaNodes));
    }

}
