package org.ovirt.engine.core.bll.scheduling.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.utils.NumaTestUtils;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
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

    @Test
    public void testCpuPinningLowCpus() {
        VM vm = new VM();
        vm.setCurrentSockets(1);
        vm.setCurrentThreadsPerCore(1);
        vm.setCurrentCoresPerSocket(1);

        VDS host = createVDS();

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNodeWithCpus(0, 1500,
                        IntStream.concat(IntStream.range(0, 16), IntStream.range(32, 48)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(1, 1500,
                        IntStream.concat(IntStream.range(16, 32), IntStream.range(48, 64)).boxed().collect(Collectors.toList()))
        );

        assertThat(NumaPinningHelper.getSapHanaCpuPinning(vm, host.getDynamicData(), hostNodes)).isNull();
    }

    @Test
    public void testCpuPinningPartial() {
        VM vm = new VM();
        vm.setCurrentSockets(8);
        vm.setCurrentThreadsPerCore(1);
        vm.setCurrentCoresPerSocket(1);

        VDS host = createVDS();

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNodeWithCpus(0, 1500,
                        IntStream.concat(IntStream.range(0, 16), IntStream.range(32, 48)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(1, 1500,
                        IntStream.concat(IntStream.range(16, 32), IntStream.range(48, 64)).boxed().collect(Collectors.toList()))
        );
        String output = NumaPinningHelper.getSapHanaCpuPinning(vm, host.getDynamicData(), hostNodes);

        assertEquals("0#1,33_1#1,33_2#2,34_3#2,34_4#17,49_5#17,49_6#18,50_7#18,50", output);
    }

    @Test
    public void testCpuPinningFull() {
        VM vm = new VM();
        vm.setCurrentSockets(2);
        vm.setCurrentThreadsPerCore(2);
        vm.setCurrentCoresPerSocket(15);

        VDS host = createVDS();

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNodeWithCpus(0, 1500,
                        IntStream.concat(IntStream.range(0, 16), IntStream.range(32, 48)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(1, 1500,
                        IntStream.concat(IntStream.range(16, 32), IntStream.range(48, 64)).boxed().collect(Collectors.toList()))
        );

        String output = NumaPinningHelper.getSapHanaCpuPinning(vm, host.getDynamicData(), hostNodes);
        assertEquals("0#1,33_1#1,33_2#2,34_3#2,34_4#3,35_5#3,35_6#4,36_7#4,36_8#5,37_9#5,37_10#6,38_11#6,38_"
                + "12#7,39_13#7,39_14#8,40_15#8,40_16#9,41_17#9,41_18#10,42_19#10,42_20#11,43_21#11,43_22#12,44_"
                + "23#12,44_24#13,45_25#13,45_26#14,46_27#14,46_28#15,47_29#15,47_30#17,49_31#17,49_32#18,50_"
                + "33#18,50_34#19,51_35#19,51_36#20,52_37#20,52_38#21,53_39#21,53_40#22,54_41#22,54_42#23,55_"
                + "43#23,55_44#24,56_45#24,56_46#25,57_47#25,57_48#26,58_49#26,58_50#27,59_51#27,59_52#28,60_"
                + "53#28,60_54#29,61_55#29,61_56#30,62_57#30,62_58#31,63_59#31,63", output);
    }

    @Test
    public void testCpuPinningFullMultipleNumas() {
        VM vm = new VM();
        vm.setCurrentSockets(1);
        vm.setCurrentThreadsPerCore(2);
        vm.setCurrentCoresPerSocket(23);

        VDS host = createVDS();

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNodeWithCpus(0, 1500,
                        IntStream.concat(IntStream.range(0, 6), IntStream.range(24, 30)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(1, 1500,
                        IntStream.concat(IntStream.range(6, 12), IntStream.range(30, 36)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(2, 1500,
                        IntStream.concat(IntStream.range(12, 18), IntStream.range(36, 42)).boxed().collect(Collectors.toList())),
                createHostNumaNodeWithCpus(3, 1500,
                        IntStream.concat(IntStream.range(18, 24), IntStream.range(42, 48)).boxed().collect(Collectors.toList()))
        );

        String output = NumaPinningHelper.getSapHanaCpuPinning(vm, host.getDynamicData(), hostNodes);
        assertEquals("0#1,25_1#1,25_2#2,26_3#2,26_4#3,27_5#3,27_6#4,28_7#4,28_8#5,29_9#5,29_10#7,31_11#7,31_"
                + "12#8,32_13#8,32_14#9,33_15#9,33_16#10,34_17#10,34_18#11,35_19#11,35_20#13,37_21#13,37_22#14,38_"
                + "23#14,38_24#15,39_25#15,39_26#16,40_27#16,40_28#17,41_29#17,41_30#19,43_31#19,43_32#20,44_"
                + "33#20,44_34#21,45_35#21,45_36#22,46_37#22,46_38#23,47_39#23,47", output);
    }

    @Test
    public void testCpuPinningSingleThreadHardware() {
        VM vm = new VM();
        vm.setCurrentSockets(2);
        vm.setCurrentThreadsPerCore(1);
        vm.setCurrentCoresPerSocket(5);

        VDS host = new VDS();
        host.setCpuSockets(2);
        host.setCpuThreads(6);
        host.setCpuCores(6);

        List<VdsNumaNode> hostNodes = Arrays.asList(
                createHostNumaNodeWithCpus(0, 1500, Arrays.asList(0, 2, 4, 6, 8, 10)),
                createHostNumaNodeWithCpus(1, 1500, Arrays.asList(1, 3, 5, 7, 9, 11))
        );

        String output = NumaPinningHelper.getSapHanaCpuPinning(vm, host.getDynamicData(), hostNodes);
        assertEquals("0#2_1#4_2#6_3#8_4#10_5#3_6#5_7#7_8#9_9#11", output);
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
        vm.getvNumaNodeList().forEach(node -> node.setNumaTuneMode(NumaTuneMode.STRICT));

        return Collections.singletonList(vm);
    }

    private VDS createVDS() {
        VDS host = new VDS();
        host.setCpuSockets(2);
        host.setCpuThreads(64);
        host.setCpuCores(32);
        return host;
    }
     private VdsNumaNode createHostNumaNodeWithCpus(int index, long freeMem, List<Integer> cpus) {
        VdsNumaNode vdsNumaNode= createHostNumaNode(index, freeMem);
        vdsNumaNode.setCpuIds(cpus);
        return vdsNumaNode;
     }
}
