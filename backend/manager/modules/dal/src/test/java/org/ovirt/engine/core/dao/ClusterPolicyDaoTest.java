package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;

public class ClusterPolicyDaoTest extends BaseDaoTestCase {

    private static final int NUMBER_OF_CLUSTER_POLICIES = 2;

    private ClusterPolicyDao dao;
    private ClusterPolicy existingPolicy;
    private ClusterPolicy dummyPolicy;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getClusterPolicyDao();
        existingPolicy = dao.get(FixturesTool.CLUSTER_POLICY_EVEN_DISTRIBUTION, Collections.emptyMap());
        createDummyPolicy();
    };

    @Test
    public void testGet() {
        ClusterPolicy result = dao.get(existingPolicy.getId(), Collections.emptyMap());

        assertTrue(result.equals(existingPolicy));
    }

    @Test
    public void testGetNegative() {
        ClusterPolicy result = dao.get(Guid.newGuid(), Collections.emptyMap());

        assertNull(result);
    }

    @Test
    public void testGetAll() {
        List<ClusterPolicy> result = dao.getAll(Collections.emptyMap());

        assertNotNull(result);
        assertEquals(result.size(), NUMBER_OF_CLUSTER_POLICIES);
    }

    @Test
    public void testSave() {
        dao.save(dummyPolicy);
        ClusterPolicy result = dao.get(dummyPolicy.getId(), Collections.emptyMap());
        assertTrue(result.equals(dummyPolicy));
        dao.remove(dummyPolicy.getId());
    }

    @Test
    public void testUpdate() {
        dao.save(dummyPolicy);
        dummyPolicy.setName("Altered dummy policy");
        dao.update(dummyPolicy);
        ClusterPolicy result = dao.get(dummyPolicy.getId(), Collections.emptyMap());
        assertTrue(result.equals(dummyPolicy));
    }

    @Test
    public void testRemove() {
        dao.save(dummyPolicy);
        dao.remove(dummyPolicy.getId());
        ClusterPolicy result = dao.get(dummyPolicy.getId(), Collections.emptyMap());
        assertNull(result);

    }

    private void createDummyPolicy() {
        dummyPolicy = new ClusterPolicy();
        dummyPolicy.setId(Guid.newGuid());
        dummyPolicy.setName("Dummy policy");
        dummyPolicy.setDescription("Dummy policy description");
        dummyPolicy.setLocked(false);
        ArrayList<Guid> filters = new ArrayList<>();
        filters.add(FixturesTool.POLICY_UNIT_MIGRATION);
        dummyPolicy.setFilters(filters);
        HashMap<Guid, Integer> filterPositionMap = new HashMap<>();
        filterPositionMap.put(FixturesTool.POLICY_UNIT_MIGRATION, 1);
        dummyPolicy.setFilterPositionMap(filterPositionMap);
        dummyPolicy.setParameterMap(new LinkedHashMap<>());
    }
}
