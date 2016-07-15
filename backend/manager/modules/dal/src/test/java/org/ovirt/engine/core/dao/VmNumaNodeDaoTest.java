package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class VmNumaNodeDaoTest extends BaseDaoTestCase {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid ANOTHER_EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    private VmNumaNodeDao vmNumaNodeDao;
    private NumaNodeStatistics newNodeStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vmNumaNodeDao = dbFacade.getVmNumaNodeDao();
        newNodeStatistics = new NumaNodeStatistics();
        newNodeStatistics.setCpuUsagePercent(20);
        newNodeStatistics.setMemUsagePercent(50);
    }

    @Test
    public void testGetAllVmNumaNodeByVmId() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(EXISTING_VM_ID);

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
        Guid vdsGroupId = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        Guid vmId = Guid.createGuidFromString("77296e00-0cad-4e5a-9299-008a7b6f4354");
        Map<Guid, List<VmNumaNode>> result = vmNumaNodeDao.getVmNumaNodeInfoByClusterId(vdsGroupId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(vmId).size());
    }

    @Test
    public void testMassSaveNumaNode() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
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
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.getVdsNumaNodeList().add(1);
        newVmNode.add(newVmNumaNode);

        vmNumaNodeDao.massSaveNumaNode(newVmNode, ANOTHER_EXISTING_VM_ID);
        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(0, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).intValue());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).intValue());

        List<Guid> vmNodeList = new ArrayList<>();
        vmNodeList.add(vmNumaNode1);
        vmNodeList.add(vmNumaNode2);
        vmNumaNodeDao.massRemoveNumaNodeByNumaNodeId(vmNodeList);

        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMassUpdateNumaNode() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
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
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.getVdsNumaNodeList().add(1);
        newVmNode.add(newVmNumaNode);

        vmNumaNodeDao.massSaveNumaNode(newVmNode, ANOTHER_EXISTING_VM_ID);
        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        nodes.get(vmNumaNode1).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode1).getVdsNumaNodeList().add(1);

        nodes.get(vmNumaNode2).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode2).getVdsNumaNodeList().add(0);

        newVmNode.clear();
        newVmNode.add(nodes.get(vmNumaNode1));
        newVmNode.add(nodes.get(vmNumaNode2));

        vmNumaNodeDao.massUpdateNumaNode(newVmNode);

        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        nodes.clear();
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).intValue());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).intValue());

        List<Guid> vmNodeList = new ArrayList<>();
        vmNodeList.add(vmNumaNode1);
        vmNodeList.add(vmNumaNode2);
        vmNumaNodeDao.massRemoveNumaNodeByNumaNodeId(vmNodeList);

        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
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
