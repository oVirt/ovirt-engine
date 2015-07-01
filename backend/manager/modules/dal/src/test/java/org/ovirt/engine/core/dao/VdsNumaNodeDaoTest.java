/**
 *
 */
package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 *
 */
public class VdsNumaNodeDaoTest extends BaseDaoTestCase {

    private static final Guid ANOTHER_EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    private VdsNumaNodeDao vdsNumaNodeDao;
    private VdsStaticDao vdsStaticDao;
    private VdsStatic existingVds;
    private NumaNodeStatistics newNodeStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vdsNumaNodeDao = dbFacade.getVdsNumaNodeDao();
        vdsStaticDao = dbFacade.getVdsStaticDao();
        existingVds = vdsStaticDao.get(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"));
        newNodeStatistics = new NumaNodeStatistics();
        newNodeStatistics.setCpuUsagePercent(20);
        newNodeStatistics.setMemUsagePercent(50);
    }

    @Test
    public void testGetAllVdsNumaNodeByVdsId() {
        List<VdsNumaNode> result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(existingVds.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testMassSaveNumaNode() {
        List<VdsNumaNode> result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        List<VdsNumaNode> newVdsNode = new ArrayList<>();
        VdsNumaNode newVdsNumaNode= new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(0, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(0);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 0));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNode.add(newVdsNumaNode);

        newVdsNumaNode= new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(4, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(1);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 1));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNode.add(newVdsNumaNode);

        vdsNumaNodeDao.massSaveNumaNode(newVdsNode, ANOTHER_EXISTING_VDS_ID, null);
        result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testMassUpdateNumaNode() {
        List<VdsNumaNode> result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        Set<Integer> cpuList = new HashSet<>();
        cpuList.addAll(result.get(0).getCpuIds());
        cpuList.addAll(result.get(1).getCpuIds());
        Set<Integer> expectedCpuList = new HashSet<>();
        expectedCpuList.addAll(generateCpuList(0, 4));
        assertEquals(expectedCpuList, cpuList);

        result.get(0).setCpuIds(generateCpuList(4, 2));
        result.get(1).setCpuIds(generateCpuList(6, 2));
        vdsNumaNodeDao.massUpdateNumaNode(result);

        result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        cpuList.clear();
        cpuList.addAll(result.get(0).getCpuIds());
        cpuList.addAll(result.get(1).getCpuIds());
        expectedCpuList.clear();
        expectedCpuList.addAll(generateCpuList(4, 4));
        assertEquals(expectedCpuList, cpuList);
    }

    @Test
    public void testMassUpdateNumaNodeStatistics() {
        List<VdsNumaNode> result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(47607, result.get(0).getNumaNodeStatistics().getMemFree());
        assertEquals(47607, result.get(1).getNumaNodeStatistics().getMemFree());

        result.get(0).getNumaNodeStatistics().setMemFree(50000);
        result.get(1).getNumaNodeStatistics().setMemFree(50000);
        vdsNumaNodeDao.massUpdateNumaNodeStatistics(result);

        result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50000, result.get(0).getNumaNodeStatistics().getMemFree());
        assertEquals(50000, result.get(1).getNumaNodeStatistics().getMemFree());
    }

    @Test
    public void testMassRemoveNumaNodeByNumaNodeId() {
        List<VdsNumaNode> newVdsNode = new ArrayList<>();
        VdsNumaNode newVdsNumaNode = new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(0, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(0);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 0));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNode.add(newVdsNumaNode);

        newVdsNumaNode = new VdsNumaNode();
        newVdsNumaNode.setCpuIds(generateCpuList(4, 4));
        newVdsNumaNode.setId(Guid.newGuid());
        newVdsNumaNode.setIndex(1);
        newVdsNumaNode.setNumaNodeDistances(generateDistance(2, 1));
        newVdsNumaNode.setNumaNodeStatistics(newNodeStatistics);
        newVdsNode.add(newVdsNumaNode);

        vdsNumaNodeDao.massSaveNumaNode(newVdsNode, ANOTHER_EXISTING_VDS_ID, null);

        List<VdsNumaNode> result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(2, result.size());

        List<Guid> numaNodeIds = new ArrayList<>();
        numaNodeIds.add(result.get(0).getId());
        numaNodeIds.add(result.get(1).getId());

        vdsNumaNodeDao.massRemoveNumaNodeByNumaNodeId(numaNodeIds);

        result = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(ANOTHER_EXISTING_VDS_ID);
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
