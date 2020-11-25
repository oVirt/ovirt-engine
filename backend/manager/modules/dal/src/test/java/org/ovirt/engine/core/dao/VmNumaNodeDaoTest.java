package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class VmNumaNodeDaoTest extends BaseDaoTestCase<VmNumaNodeDao> {

    private static final Guid EXISTING_VM_ID = FixturesTool.VM_RHEL5_POOL_50;
    private static final Guid ANOTHER_EXISTING_VM_ID = FixturesTool.VM_RHEL5_POOL_57;

    private NumaNodeStatistics newNodeStatistics;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        newNodeStatistics = new NumaNodeStatistics();
        newNodeStatistics.setCpuUsagePercent(20);
        newNodeStatistics.setMemUsagePercent(50);
    }

    @Test
    public void testGetAllVmNumaNodeByVmId() {
        List<VmNumaNode> result = dao.getAllVmNumaNodeByVmId(EXISTING_VM_ID);

        assertNotNull(result);
        assertEquals(2, result.size());

        Guid vmNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b517");
        Guid vmNumaNode2 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b518");

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(2, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertTrue(nodes.get(vmNumaNode1).getVdsNumaNodeList().contains(0));
        assertTrue(nodes.get(vmNumaNode1).getVdsNumaNodeList().contains(1));

        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
    }

    @Test
    public void testGetVmNumaNodeInfoByBdsGroupId() {
        Guid vdsGroupId = FixturesTool.CLUSTER;
        Guid vmId = FixturesTool.VM_RHEL5_POOL_50;
        Map<Guid, List<VmNumaNode>> result = dao.getVmNumaNodeInfoByClusterId(vdsGroupId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(vmId).size());
    }

    @Test
    public void testMassSaveNumaNode() {
        List<VmNumaNode> result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        Guid vmNumaNode1 = Guid.newGuid();
        Guid vmNumaNode2 = Guid.newGuid();

        List<VmNumaNode> newVmNode = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(vmNumaNode1);
        newVmNumaNode.setIndex(0);
        newVmNumaNode.getVdsNumaNodeList().add(0);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.STRICT);
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.getVdsNumaNodeList().add(1);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.INTERLEAVE);
        newVmNode.add(newVmNumaNode);

        dao.massSaveNumaNode(newVmNode, ANOTHER_EXISTING_VM_ID);
        result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(0, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).intValue());
        assertEquals(NumaTuneMode.STRICT, nodes.get(vmNumaNode1).getNumaTuneMode());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).intValue());
        assertEquals(NumaTuneMode.INTERLEAVE, nodes.get(vmNumaNode2).getNumaTuneMode());

        List<Guid> vmNodeList = new ArrayList<>();
        vmNodeList.add(vmNumaNode1);
        vmNodeList.add(vmNumaNode2);
        dao.massRemoveNumaNodeByNumaNodeId(vmNodeList);

        result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMassUpdateNumaNode() {
        List<VmNumaNode> result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        Guid vmNumaNode1 = Guid.newGuid();
        Guid vmNumaNode2 = Guid.newGuid();

        List<VmNumaNode> newVmNode = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(vmNumaNode1);
        newVmNumaNode.setIndex(0);
        newVmNumaNode.getVdsNumaNodeList().add(0);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.STRICT);
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.getVdsNumaNodeList().add(1);
        newVmNumaNode.setNumaTuneMode(NumaTuneMode.STRICT);
        newVmNode.add(newVmNumaNode);

        dao.massSaveNumaNode(newVmNode, ANOTHER_EXISTING_VM_ID);
        result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        nodes.get(vmNumaNode1).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode1).getVdsNumaNodeList().add(1);
        nodes.get(vmNumaNode1).setNumaTuneMode(NumaTuneMode.INTERLEAVE);

        nodes.get(vmNumaNode2).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode2).getVdsNumaNodeList().add(0);
        nodes.get(vmNumaNode2).setNumaTuneMode(NumaTuneMode.PREFERRED);

        newVmNode.clear();
        newVmNode.add(nodes.get(vmNumaNode1));
        newVmNode.add(nodes.get(vmNumaNode2));

        dao.massUpdateNumaNode(newVmNode);

        result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        nodes.clear();
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).intValue());
        assertEquals(NumaTuneMode.INTERLEAVE, nodes.get(vmNumaNode1).getNumaTuneMode());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).intValue());
        assertEquals(NumaTuneMode.PREFERRED, nodes.get(vmNumaNode2).getNumaTuneMode());

        List<Guid> vmNodeList = new ArrayList<>();
        vmNodeList.add(vmNumaNode1);
        vmNodeList.add(vmNumaNode2);
        dao.massRemoveNumaNodeByNumaNodeId(vmNodeList);

        result = dao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private List<Integer> generateCpuList(int fromIndex, int count) {
        List<Integer> cpuList = new ArrayList<>(count);
        for (int i = fromIndex; i < (fromIndex + count); i++) {
            cpuList.add(i);
        }
        return cpuList;
    }

    private Map<Integer, Integer> generateDistance(int nodeCount, int selfNodeIndex) {
        Map<Integer, Integer> distance = new HashMap<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            distance.put(i, (Math.abs(selfNodeIndex - i) + 1) * 10);
        }
        return distance;
    }

}
