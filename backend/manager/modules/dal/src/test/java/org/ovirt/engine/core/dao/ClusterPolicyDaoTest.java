package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;

public class ClusterPolicyDaoTest extends BaseDaoTestCase<ClusterPolicyDao> {

    private static final int NUMBER_OF_CLUSTER_POLICIES = 2;

    private ClusterPolicy existingPolicy;
    private ClusterPolicy dummyPolicy;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingPolicy = dao.get(FixturesTool.CLUSTER_POLICY_EVEN_DISTRIBUTION, Collections.emptyMap());
        createDummyPolicy();
    }

    @Test
    public void testGet() {
        ClusterPolicy result = dao.get(existingPolicy.getId(), Collections.emptyMap());

        assertEquals(result, existingPolicy);
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
        assertEquals(NUMBER_OF_CLUSTER_POLICIES, result.size());
    }

    @Test
    public void testSave() {
        dao.save(dummyPolicy);
        ClusterPolicy result = dao.get(dummyPolicy.getId(), Collections.emptyMap());
        assertEquals(result, dummyPolicy);
        dao.remove(dummyPolicy.getId());
    }

    @Test
    public void testUpdate() {
        dao.save(dummyPolicy);
        dummyPolicy.setName("Altered dummy policy");
        dao.update(dummyPolicy);
        ClusterPolicy result = dao.get(dummyPolicy.getId(), Collections.emptyMap());
        assertEquals(result, dummyPolicy);
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
        dummyPolicy.setFilters(Collections.singletonList(FixturesTool.POLICY_UNIT_MIGRATION));
        dummyPolicy.setFilterPositionMap(Collections.singletonMap(FixturesTool.POLICY_UNIT_MIGRATION, 1));
        dummyPolicy.setParameterMap(new LinkedHashMap<>());
    }
}
