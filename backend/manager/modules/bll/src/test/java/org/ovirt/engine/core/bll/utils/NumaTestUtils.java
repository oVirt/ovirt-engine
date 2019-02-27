package org.ovirt.engine.core.bll.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
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
        vmNumaNode.setId(Guid.newGuid());
        vmNumaNode.setIndex(index);
        vmNumaNode.setMemTotal(1000);
        return vmNumaNode;
    }

    public static VmNumaNode createVmNumaNode(int index, List<VdsNumaNode> vdsNumaNodes) {
        VmNumaNode numaNode = createVmNumaNode(index);
        final List<Integer> numaPinning = new ArrayList<>();
        for (VdsNumaNode vdsNumaNode : vdsNumaNodes) {
            numaPinning.add(vdsNumaNode.getIndex());
        }
        numaNode.setVdsNumaNodeList(numaPinning);
        return numaNode;
    }

    public static void mockVdsNumaNodeDao(final VdsNumaNodeDao vdsNumaNodeDao, VdsNumaNode... vdsNumaNodes) {
        mockVdsNumaNodeDao(vdsNumaNodeDao, Arrays.asList(vdsNumaNodes));
    }

    public static void mockVdsNumaNodeDao(final VdsNumaNodeDao vdsNumaNodeDao, List<VdsNumaNode> vdsNumaNodes) {
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any())).thenReturn(vdsNumaNodes);
    }

    public static void mockVdsDao(final VdsDao vdsDao) {
        when(vdsDao.get(any())).thenReturn(new VDS());
    }

    public static void mockVmNumaNodeDao(final VmNumaNodeDao vmNumaNodeDao, List<VmNumaNode> vmNumaNodes) {
        when(vmNumaNodeDao.getAllVmNumaNodeByVmId(any())).thenReturn(vmNumaNodes);
    }

    public static void mockVmNumaNodeDao(final VmNumaNodeDao vmNumaNodeDao, VmNumaNode... vmNumaNodes) {
        mockVmNumaNodeDao(vmNumaNodeDao, Arrays.asList(vmNumaNodes));
    }

}
