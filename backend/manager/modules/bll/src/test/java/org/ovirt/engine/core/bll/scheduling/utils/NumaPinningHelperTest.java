package org.ovirt.engine.core.bll.scheduling.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.utils.NumaTestUtils;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

class NumaPinningHelperTest {

    @Test
    public void testFindAssignmentNoNodes() {
        Optional<Map<Guid, Integer>> assignment = NumaPinningHelper.findAssignment(
                Collections.emptyList(), Collections.emptyList(), false);

        assertThat(assignment.isPresent()).isFalse();
    }

    @Test
    public void testFindAssignmentNoSpaceOnHost() {
        List<VmNumaNode> vmNodes = Arrays.asList(
                createVmNumaNode(0, Arrays.asList(0, 1)),
                createVmNumaNode(1, Arrays.asList(0, 1))
        );

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNode(0, 512),
                createHostNumaNode(1, 512)
        );

        Optional<Map<Guid, Integer>> assignment = NumaPinningHelper.findAssignment(createVms(vmNodes), hostNodes, false);

        assertThat(assignment.isPresent()).isFalse();
    }

    @Test
    public void testFindAssignment() {
        List<VmNumaNode> vmNodes = Arrays.asList(
                createVmNumaNode(0, Arrays.asList(0, 1)),
                createVmNumaNode(1, Arrays.asList(0, 1)),
                createVmNumaNode(2, Arrays.asList(0, 1)),
                createVmNumaNode(3, Arrays.asList(0, 1))
        );

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNode(0, 2500),
                createHostNumaNode(1, 2500)
        );

        Optional<Map<Guid, Integer>> assignment = NumaPinningHelper.findAssignment(createVms(vmNodes), hostNodes, false);

        assertThat(assignment.isPresent()).isTrue();
        assertThat(assignment.get().entrySet()).extracting("key", "value").containsOnly(
                tuple(vmNodes.get(0).getId(), 1),
                tuple(vmNodes.get(1).getId(), 1),
                tuple(vmNodes.get(2).getId(), 0),
                tuple(vmNodes.get(3).getId(), 0)
        );
    }

    @Test
    public void testFindAssignmentWithBacktracking() {
        // The pinning is chosen such that backtracking will be needed to find the assignemnt
        List<VmNumaNode> vmNodes = Arrays.asList(
                createVmNumaNode(0, Arrays.asList(0, 1, 2, 3)),
                createVmNumaNode(1, Arrays.asList(1, 2, 3)),
                createVmNumaNode(2, Arrays.asList(1, 2, 3)),
                createVmNumaNode(3, Arrays.asList(1, 2, 3))
        );

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNode(0, 1500),
                createHostNumaNode(1, 1500),
                createHostNumaNode(2, 1500),
                createHostNumaNode(3, 1500)
        );

        Optional<Map<Guid, Integer>> assignment = NumaPinningHelper.findAssignment(createVms(vmNodes), hostNodes, false);

        assertThat(assignment.isPresent()).isTrue();
        assertThat(assignment.get().entrySet()).extracting("key", "value").containsOnly(
                tuple(vmNodes.get(0).getId(), 0),
                tuple(vmNodes.get(1).getId(), 3),
                tuple(vmNodes.get(2).getId(), 2),
                tuple(vmNodes.get(3).getId(), 1)
        );
    }

    // TODO - add tests for multiple VMs

    private VmNumaNode createVmNumaNode(int index, List<Integer> hostNodeIndices) {
        VmNumaNode node = NumaTestUtils.createVmNumaNode(index);
        node.setVdsNumaNodeList(hostNodeIndices);
        return node;
    }

    private VdsNumaNode createHostNumaNode(int index, long freeMem) {
        VdsNumaNode node = NumaTestUtils.createVdsNumaNode(index);
        node.setNumaNodeStatistics(new NumaNodeStatistics());
        node.getNumaNodeStatistics().setMemFree(freeMem);
        return node;
    }

    private List<VM> createVms(List<VmNumaNode> nodes) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setvNumaNodeList(nodes);
        vm.setNumaTuneMode(NumaTuneMode.STRICT);

        return Collections.singletonList(vm);
    }
}
