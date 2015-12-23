/**
 *
 */
package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 *
 */
public class VmNumaNodeDaoTest extends BaseDaoTestCase {

    private static final Guid ANOTHER_EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    private VmNumaNodeDao vmNumaNodeDao;
    private VmStaticDao vmStaticDao;
    private VmStatic existingVm;
    private NumaNodeStatistics newNodeStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vmNumaNodeDao = dbFacade.getVmNumaNodeDao();
        vmStaticDao = dbFacade.getVmStaticDao();
        existingVm = vmStaticDao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354"));
        newNodeStatistics = new NumaNodeStatistics();
        newNodeStatistics.setCpuUsagePercent(20);
        newNodeStatistics.setMemUsagePercent(50);
    }

    @Test
    public void testGetAllVmNumaNodeByVmId() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(existingVm.getId());

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
        assertEquals(true, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(true, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(1).getSecond().getFirst());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(false, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());
    }

    @Test
    public void testGetAllPinnedVmNumaNodeByVdsNumaNodeId() {
        Guid vdsNumaNodeId = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b515");
        Guid vmNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b517");
        List<VmNumaNode> result = vmNumaNodeDao.getAllPinnedVmNumaNodeByVdsNumaNodeId(vdsNumaNodeId);

        /*
         * Assert retrieved correct vmNUMAnode
         */
        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(vmNumaNode1, result.get(0).getId());
        validateCorrectVdsNumaNode(vdsNumaNodeId, result.get(0).getVdsNumaNodeList());
        assertEquals(true, result.get(0).getVdsNumaNodeList().get(0).getSecond().getFirst());
        validateCorrectIndexNumaNode(0, result.get(0).getVdsNumaNodeList());
    }

    private void validateCorrectVdsNumaNode(Guid vdsNumaNodeId, List<Pair<Guid, Pair<Boolean, Integer>>> pairsList) {
        List<Guid> hostIds = new LinkedList<>();
        for (Pair<Guid, Pair<Boolean, Integer>> pair : pairsList){
            hostIds.add(pair.getFirst());
        }
        assertTrue("correct vdsNumaNode", hostIds.contains(vdsNumaNodeId));
    }

    private void validateCorrectIndexNumaNode(int expectedIndex, List<Pair<Guid, Pair<Boolean, Integer>>> pairsList) {
        List<Integer> indexes = new LinkedList<>();
        for (Pair<Guid, Pair<Boolean, Integer>> pair : pairsList){
            indexes.add(pair.getSecond().getSecond());
        }
        assertTrue("correct index", indexes.contains(expectedIndex));
    }

    @Test
    public void testGetAllVmNumaNodeByVdsNumaNodeId() {
        Guid vdsNumaNodeId = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b515");
        Guid vmNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b517");
        Guid vmNumaNode2 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b518");
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVdsNumaNodeId(vdsNumaNodeId);

        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        validateCorrectVdsNumaNode(vdsNumaNodeId, nodes.get(vmNumaNode1).getVdsNumaNodeList());
        assertEquals(true, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getFirst());
        validateCorrectIndexNumaNode(0, nodes.get(vmNumaNode1).getVdsNumaNodeList());

        validateCorrectVdsNumaNode(vdsNumaNodeId, nodes.get(vmNumaNode2).getVdsNumaNodeList());
        assertEquals(false, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getFirst());
        validateCorrectIndexNumaNode(0, nodes.get(vmNumaNode2).getVdsNumaNodeList());
    }

    @Test
    public void testGetVmNumaNodeInfoByClusterId() {
        Guid clusterId = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        List<Pair<Guid, VmNumaNode>> result = vmNumaNodeDao.getVmNumaNodeInfoByClusterId(clusterId);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetVmNumaNodeInfoByBdsGroupIdAsMap() {
        Guid vdsGroupId = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        Guid vmId = Guid.createGuidFromString("77296e00-0cad-4e5a-9299-008a7b6f4354");
        Map<Guid, List<VmNumaNode>> result = vmNumaNodeDao.getVmNumaNodeInfoByClusterIdAsMap(vdsGroupId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(vmId).size());
    }

    @Test
    public void testGetPinnedNumaNodeIndex() {
        List<Pair<Guid, Integer>> result = vmNumaNodeDao.getPinnedNumaNodeIndex(existingVm.getId());

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testMassSaveNumaNode() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        Guid vdsNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b515");
        Guid vdsNumaNode2 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b516");
        Guid vmNumaNode1 = Guid.newGuid();
        Guid vmNumaNode2 = Guid.newGuid();

        List<VmNumaNode> newVmNode = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(vmNumaNode1);
        newVmNumaNode.setIndex(0);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode1, new Pair<>(true, 0)));
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode2, new Pair<>(true, 1)));
        newVmNode.add(newVmNumaNode);

        vmNumaNodeDao.massSaveNumaNode(newVmNode, null, ANOTHER_EXISTING_VM_ID);
        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(true, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode1, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(0, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(true, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode2, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

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

        Guid vdsNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b515");
        Guid vdsNumaNode2 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b516");
        Guid vmNumaNode1 = Guid.newGuid();
        Guid vmNumaNode2 = Guid.newGuid();

        List<VmNumaNode> newVmNode = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(vmNumaNode1);
        newVmNumaNode.setIndex(0);
        newVmNumaNode.setNumaNodeDistances(generateDistance(2, 0));
        newVmNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode1, new Pair<>(true, 0)));
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.setNumaNodeDistances(generateDistance(2, 1));
        newVmNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode2, new Pair<>(true, 1)));
        newVmNode.add(newVmNumaNode);

        vmNumaNodeDao.massSaveNumaNode(newVmNode, null, ANOTHER_EXISTING_VM_ID);
        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        nodes.get(vmNumaNode1).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode1).getVdsNumaNodeList().add(new Pair<>(vdsNumaNode2, new Pair<>(true, 1)));

        nodes.get(vmNumaNode2).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode2).getVdsNumaNodeList().add(new Pair<>(vdsNumaNode1, new Pair<>(true, 0)));

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
        assertEquals(true, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode2, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(true, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode1, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

        List<Guid> vmNodeList = new ArrayList<>();
        vmNodeList.add(vmNumaNode1);
        vmNodeList.add(vmNumaNode2);
        vmNumaNodeDao.massRemoveNumaNodeByNumaNodeId(vmNodeList);

        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMassUpdateVmNumaNodeRuntimePinning() {
        List<VmNumaNode> result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        Guid vdsNumaNode1 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b515");
        Guid vdsNumaNode2 = new Guid("3c2b81e6-5080-4ad1-86a1-cf513b15b516");
        Guid vmNumaNode1 = Guid.newGuid();
        Guid vmNumaNode2 = Guid.newGuid();

        List<VmNumaNode> newVmNode = new ArrayList<>();
        VmNumaNode newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(0, 4));
        newVmNumaNode.setId(vmNumaNode1);
        newVmNumaNode.setIndex(0);
        newVmNumaNode.setNumaNodeDistances(generateDistance(2, 0));
        newVmNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode1, new Pair<>(false, 0)));
        newVmNode.add(newVmNumaNode);

        newVmNumaNode = new VmNumaNode();
        newVmNumaNode.setCpuIds(generateCpuList(4, 4));
        newVmNumaNode.setId(vmNumaNode2);
        newVmNumaNode.setIndex(1);
        newVmNumaNode.setNumaNodeDistances(generateDistance(2, 1));
        newVmNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVmNumaNode.getVdsNumaNodeList().add(new Pair<>(vdsNumaNode2, new Pair<>(false, 1)));
        newVmNode.add(newVmNumaNode);

        vmNumaNodeDao.massSaveNumaNode(newVmNode, null, ANOTHER_EXISTING_VM_ID);
        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Guid, VmNumaNode> nodes = new HashMap<>(2);
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        nodes.get(vmNumaNode1).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode1).getVdsNumaNodeList().add(new Pair<>(vdsNumaNode2, new Pair<>(false, 1)));

        nodes.get(vmNumaNode2).getVdsNumaNodeList().clear();
        nodes.get(vmNumaNode2).getVdsNumaNodeList().add(new Pair<>(vdsNumaNode1, new Pair<>(false, 0)));

        List<VmNumaNode> updateNodes = new ArrayList<>();
        updateNodes.add(nodes.get(vmNumaNode1));
        updateNodes.add(nodes.get(vmNumaNode2));

        vmNumaNodeDao.massUpdateVmNumaNodeRuntimePinning(updateNodes);

        result = vmNumaNodeDao.getAllVmNumaNodeByVmId(ANOTHER_EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        nodes.clear();
        nodes.put(result.get(0).getId(), result.get(0));
        nodes.put(result.get(1).getId(), result.get(1));

        assertTrue(nodes.containsKey(vmNumaNode1));
        assertTrue(nodes.containsKey(vmNumaNode2));

        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().size());
        assertEquals(false, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode2, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(1, nodes.get(vmNumaNode1).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

        assertEquals(1, nodes.get(vmNumaNode2).getVdsNumaNodeList().size());
        assertEquals(false, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getFirst());
        assertEquals(vdsNumaNode1, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getFirst());
        assertEquals(0, nodes.get(vmNumaNode2).getVdsNumaNodeList().get(0).getSecond().getSecond().intValue());

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
