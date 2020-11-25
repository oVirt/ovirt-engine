package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class NumaSettingFactoryTest {

    private static NumaTuneMode numaTuneMode;
    private static List<VdsNumaNode> vdsNumaNodes;
    private static List<VmNumaNode> vmNumaNodes;

    @BeforeEach
    public void setUp() {
        numaTuneMode = NumaTuneMode.INTERLEAVE;
        vdsNumaNodes = createTestVdsNumaNodes();
        vmNumaNodes = createTestVmNumaNodes();
    }

    @Test
    public void testBuildVmNumaNodeSetting() {
        List<Map<String, Object>> vmNumaNodesSetting =
                NumaSettingFactory.buildVmNumaNodeSetting(vmNumaNodes);
        assertEquals(2, vmNumaNodesSetting.size());
        assertTrue(vmNumaNodesSetting.get(0).containsKey(VdsProperties.NUMA_NODE_INDEX));
        assertEquals(0, vmNumaNodesSetting.get(0).get(VdsProperties.NUMA_NODE_INDEX));
        assertTrue(vmNumaNodesSetting.get(0).containsKey(VdsProperties.NUMA_NODE_CPU_LIST));
        assertEquals("0-3", vmNumaNodesSetting.get(0).get(VdsProperties.NUMA_NODE_CPU_LIST));
        assertTrue(vmNumaNodesSetting.get(1).containsKey(VdsProperties.VM_NUMA_NODE_MEM));
        assertEquals("1024", vmNumaNodesSetting.get(1).get(VdsProperties.VM_NUMA_NODE_MEM));
    }

    @Test
    public void testBuildCpuPinningWithNumaSetting() {
        Map<String, Object> cpuPinning =
                NumaSettingFactory.buildCpuPinningWithNumaSetting(vmNumaNodes, vdsNumaNodes);
        assertEquals(8, cpuPinning.size());
        assertTrue(cpuPinning.containsKey("3"));
        assertEquals("0-3", cpuPinning.get("3"));
        assertTrue(cpuPinning.containsKey("7"));
        assertEquals("4-7", cpuPinning.get("7"));
    }

    @Test
    public void shouldNotCreateCpuPinningForVirtualNumaNodes() {
        for (VmNumaNode numaNode : vmNumaNodes) {
            numaNode.getVdsNumaNodeList().clear();
        }
        Map<String, Object> cpuPinning =
                NumaSettingFactory.buildCpuPinningWithNumaSetting(vmNumaNodes, vdsNumaNodes);
        assertThat(cpuPinning).isEmpty();
        Map<String, Object> mapping =
                NumaSettingFactory.buildVmNumatuneSetting(vmNumaNodes);
        assertThat(mapping).doesNotContainKeys(VdsProperties.NUMA_TUNE_MODE, VdsProperties.NUMA_TUNE_NODESET);
    }

    @Test
    public void testBuildVmNumatuneSetting() {
        Map<String, Object> numaTune =
                NumaSettingFactory.buildVmNumatuneSetting(vmNumaNodes);
        assertEquals(1, numaTune.size());

        assertTrue(numaTune.containsKey(VdsProperties.NUMA_TUNE_MEMNODES));
        List<Map<String, String>> memNodes =
                (List<Map<String, String>>) numaTune.get(VdsProperties.NUMA_TUNE_MEMNODES);

        assertEquals("0", memNodes.get(0).get(VdsProperties.NUMA_TUNE_VM_NODE_INDEX));
        assertEquals("0", memNodes.get(0).get(VdsProperties.NUMA_TUNE_NODESET));
        assertEquals(NumaTuneMode.INTERLEAVE.getValue(), memNodes.get(0).get(VdsProperties.NUMA_TUNE_MODE));

        assertEquals("1", memNodes.get(1).get(VdsProperties.NUMA_TUNE_VM_NODE_INDEX));
        assertEquals("1", memNodes.get(1).get(VdsProperties.NUMA_TUNE_NODESET));
        assertEquals(NumaTuneMode.STRICT.getValue(), memNodes.get(1).get(VdsProperties.NUMA_TUNE_MODE));
    }

    private static List<VmNumaNode> createTestVmNumaNodes() {

        List<VmNumaNode> newVmNodes = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(Guid.newGuid());
        newVmNumaNode.setIndex(0);
        newVmNumaNode.setMemTotal(1024);
        newVmNumaNode.getVdsNumaNodeList().add(0);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
        newVmNodes.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(Guid.newGuid());
        newVmNumaNode.setIndex(1);
        newVmNumaNode.setMemTotal(1024);
        newVmNumaNode.getVdsNumaNodeList().add(1);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.STRICT);
        newVmNodes.add(newVmNumaNode);

        return newVmNodes;
    }

    private static List<VdsNumaNode> createTestVdsNumaNodes() {

        NumaNodeStatistics newNodeStatistics = new NumaNodeStatistics();
        newNodeStatistics.setCpuUsagePercent(20);
        newNodeStatistics.setMemUsagePercent(50);

        List<VdsNumaNode> newVdsNodes = new ArrayList<>();

        VdsNumaNode newVdsNumaNode= new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(0, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(0);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 0));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNodes.add(newVdsNumaNode);

        newVdsNumaNode= new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(4, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(1);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 1));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNodes.add(newVdsNumaNode);

        return newVdsNodes;
    }

    private static List<Integer> generateCpuList(int fromIndex, int count) {
        List<Integer> cpuList = new ArrayList<>(count);
        for (int i = fromIndex; i < (fromIndex + count); i++) {
            cpuList.add(i);
        }
        return cpuList;
    }

    private static Map<Integer, Integer> generateDistance(int nodeCount, int selfNodeIndex) {
        Map<Integer, Integer> distance = new HashMap<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            distance.put(i, (Math.abs(selfNodeIndex - i) + 1) * 10);
        }
        return distance;
    }

}
