package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AffinityGroupDaoTest extends BaseDaoTestCase {

    private static final String AFFINITY_GROUP_NAME = "affinityGroup1";
    private static final int NUM_OF_AFFINITY_GROUPS_IN_CLUSTER = 3;
    private static final int NUM_OF_AFFINITY_GROUPS_FOR_VM = 2;
    private static final int NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP = 2;

    private AffinityGroupDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getAffinityGroupDao();
    }

    @Test
    public void testGetById() {
        AffinityGroup affinityGroup = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertNotNull(affinityGroup);
        assertEquals(FixturesTool.EXISTING_AFFINITY_GROUP_ID, affinityGroup.getId());
        // empty
        affinityGroup = dao.get(null);
        assertNull(affinityGroup);
        affinityGroup = dao.get(Guid.Empty);
        assertNull(affinityGroup);
        affinityGroup = dao.get(Guid.newGuid());
        assertNull(affinityGroup);
    }

    @Test
    public void testGetByClusterId() {
        List<AffinityGroup> affinityGroupList = dao.getAllAffinityGroupsByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        assertFalse(affinityGroupList.isEmpty());
        assertEquals(NUM_OF_AFFINITY_GROUPS_IN_CLUSTER, affinityGroupList.size());
        // empty
        affinityGroupList = dao.getAllAffinityGroupsByClusterId(null);
        assertTrue(affinityGroupList.isEmpty());
        affinityGroupList = dao.getAllAffinityGroupsByClusterId(Guid.Empty);
        assertTrue(affinityGroupList.isEmpty());
        affinityGroupList = dao.getAllAffinityGroupsByClusterId(Guid.newGuid());
        assertTrue(affinityGroupList.isEmpty());
    }

    @Test
    public void testGetByVmId() {
        List<AffinityGroup> affinityGroupList = dao.getAllAffinityGroupsByVmId(FixturesTool.VM_RHEL5_POOL_50);
        assertFalse(affinityGroupList.isEmpty());
        assertEquals(NUM_OF_AFFINITY_GROUPS_FOR_VM, affinityGroupList.size());
        // empty
        affinityGroupList = dao.getAllAffinityGroupsByVmId(null);
        assertTrue(affinityGroupList.isEmpty());
        affinityGroupList = dao.getAllAffinityGroupsByVmId(Guid.Empty);
        assertTrue(affinityGroupList.isEmpty());
        affinityGroupList = dao.getAllAffinityGroupsByVmId(Guid.newGuid());
        assertTrue(affinityGroupList.isEmpty());
    }

    @Test
    public void testGetByName() {
        AffinityGroup affinityGroup = dao.getByName(AFFINITY_GROUP_NAME);
        assertNotNull(affinityGroup);
        affinityGroup = dao.getByName(AFFINITY_GROUP_NAME + "A");
        assertNull(affinityGroup);
    }

    @Test
    public void testSave() {
        AffinityGroup ag = new AffinityGroup();
        ag.setId(Guid.newGuid());
        ag.setName("testAG");
        ag.setDescription("desc");
        ag.setClusterId(FixturesTool.CLUSTER_RHEL6_NFS);
        ag.setEnforcing(false);
        ag.setPositive(false);
        ag.setEntityIds(new ArrayList<>());
        ag.getEntityIds().add(FixturesTool.VM_RHEL5_POOL_50);
        ag.setEntityNames(new ArrayList<>());
        ag.getEntityNames().add(FixturesTool.VM_RHEL5_POOL_50_NAME);
        dao.save(ag);
        AffinityGroup fetched = dao.get(ag.getId());
        assertEquals(ag, fetched);
    }

    @Test
    public void testSimpleUpdate() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        existing.setName("my_new_name");
        existing.setPositive(false);
        existing.setEnforcing(true);
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(existing, fetched);
    }

    @Test
    public void testRemoveVmsFromExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertFalse(existing.getEntityNames().isEmpty());
        existing.getEntityIds().clear();
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertTrue(fetched.getEntityNames().isEmpty());
    }

    @Test
    public void testAddVmsForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP, existing.getEntityNames().size());
        existing.getEntityIds().add(FixturesTool.VM_RHEL5_POOL_51);
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP + 1, fetched.getEntityNames().size());
    }

    @Test
    public void testRemove() {
        dao.remove(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertNull(dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID));
        assertEquals(NUM_OF_AFFINITY_GROUPS_IN_CLUSTER - 1,
                dao.getAllAffinityGroupsByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI).size());
    }

    @Test
    public void testRemoveVmFromAffinityGroups() {
        assertFalse(dao.getAllAffinityGroupsByVmId(FixturesTool.VM_RHEL5_POOL_50).isEmpty());
        dao.removeVmFromAffinityGroups(FixturesTool.VM_RHEL5_POOL_50);
        assertTrue(dao.getAllAffinityGroupsByVmId(FixturesTool.VM_RHEL5_POOL_50).isEmpty());
    }

    @Test
    public void testEmptyGetAffinityGroupByVdsId() {
        getAffinityGroupByVdsIdHelper(Guid.Empty, 0);
    }

    @Test
    public void testGetAffinityGroupByVdsId() {
        getAffinityGroupByVdsIdHelper(FixturesTool.VDS_RHEL6_NFS_SPM, 1);
    }

    private void getAffinityGroupByVdsIdHelper(Guid vdsId, int count) {
        List<AffinityGroup> affinityGroups =
                dao.getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(vdsId);

        assertNotNull(affinityGroups);
        assertEquals(count, affinityGroups.size());
    }
}
