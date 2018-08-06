package org.ovirt.engine.core.bll.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;

@ExtendWith(MockitoExtension.class)
public class NumaUtilsTest {

    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;

    @InjectMocks
    @Spy
    private NumaUtils numaUtils = new NumaUtils();

    @Test
    public void testVmFitsToSingleNode() {
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class))).thenReturn(Arrays.asList(
                createHostNode(4096, 4),
                createHostNode(1024, 1)
        ));

        VmStatic vm = createVm(2048, 2);

        assertEquals(1, numaUtils.countNumaNodesWhereVmFits(vm, Guid.newGuid()));
    }

    @Test
    public void testVmFitsToMultipleNodes() {
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class))).thenReturn(Arrays.asList(
                createHostNode(4096, 4),
                createHostNode(4096, 4),
                createHostNode(1024, 1),
                createHostNode(1024, 1)
        ));

        VmStatic vm = createVm(2048, 2);

        assertEquals(2, numaUtils.countNumaNodesWhereVmFits(vm, Guid.newGuid()));
    }

    @Test
    public void testVmDoesNotFitToAnyNode() {
        when(vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(any(Guid.class))).thenReturn(Arrays.asList(
                createHostNode(1024, 1),
                createHostNode(1024, 1)
        ));

        VmStatic vm = createVm(2048, 2);

        assertEquals(0, numaUtils.countNumaNodesWhereVmFits(vm, Guid.newGuid()));
    }

    private VmStatic createVm(int memory, int cpuCount) {
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vm.setMemSizeMb(memory);
        vm.setCpuPerSocket(cpuCount);
        return vm;
    }

    private VdsNumaNode createHostNode(int memory, int cpuCount) {
        VdsNumaNode node = new VdsNumaNode();
        node.setMemTotal(memory);
        node.setCpuIds(IntStream.range(0, cpuCount).boxed().collect(Collectors.toList()));
        return node;
    }
}
