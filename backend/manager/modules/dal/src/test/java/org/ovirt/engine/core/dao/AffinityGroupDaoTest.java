package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AffinityGroupDaoTest extends BaseDaoTestCase<AffinityGroupDao> {

    private static final String AFFINITY_GROUP_NAME = "affinityGroup1";
    private static final int NUM_OF_AFFINITY_GROUPS_IN_CLUSTER = 3;
    private static final int NUM_OF_AFFINITY_GROUPS_FOR_VM = 2;
    private static final int NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP = 2;

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
        ag.setVmEnforcing(false);
        ag.setVmAffinityRule(EntityAffinityRule.NEGATIVE);
        ag.setVdsEnforcing(false);
        ag.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
        ag.setVmIds(new ArrayList<>());
        ag.getVmIds().add(FixturesTool.VM_RHEL5_POOL_50);
        ag.setVmEntityNames(new ArrayList<>());
        ag.getVmEntityNames().add(FixturesTool.VM_RHEL5_POOL_50_NAME);
        ag.setVdsIds(new ArrayList<>());
        ag.getVdsIds().add(FixturesTool.VDS_RHEL6_NFS_SPM);
        ag.setVdsEntityNames(new ArrayList<>());
        ag.getVdsEntityNames().add(FixturesTool.GLUSTER_SERVER_NAME3);
        dao.save(ag);
        AffinityGroup fetched = dao.get(ag.getId());
        assertTrue(equals(ag, fetched));
    }

    @Test
    public void testSimpleUpdate() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        existing.setName("my_new_name");
        existing.setVmAffinityRule(EntityAffinityRule.NEGATIVE);
        existing.setVmEnforcing(true);
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertTrue(equals(existing, fetched));
    }

    @Test
    public void testRemoveVmsFromExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertFalse(existing.getVmEntityNames().isEmpty());
        existing.getVmIds().clear();
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertTrue(fetched.getVmEntityNames().isEmpty());
    }

    @Test
    public void testRemoveVdsFromExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertFalse(existing.getVdsEntityNames().isEmpty());
        existing.getVdsIds().clear();
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertTrue(fetched.getVdsEntityNames().isEmpty());
    }

    @Test
    public void testAddVmsForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP, existing.getVmEntityNames().size());
        existing.getVmIds().add(FixturesTool.VM_RHEL5_POOL_51);
        dao.update(existing);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP + 1, fetched.getVmEntityNames().size());
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

    @Test
    public void testRemoveVdsFromAffinityGroups() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertFalse(existing.getVdsIds().isEmpty());
        dao.removeVdsFromAffinityGroups(FixturesTool.VDS_RHEL6_NFS_SPM);
        assertTrue(dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID).getVdsIds().isEmpty());
    }

    private boolean equals(AffinityGroup affinityGroup, AffinityGroup other) {
        return Objects.equals(affinityGroup.getClusterId(), other.getClusterId())
                && Objects.equals(affinityGroup.getDescription(), other.getDescription())
                && affinityGroup.isVmEnforcing() == other.isVmEnforcing()
                && affinityGroup.isVdsEnforcing() == other.isVdsEnforcing()
                && Objects.equals(affinityGroup.getId(), other.getId())
                && Objects.equals(affinityGroup.getName(), other.getName())
                && affinityGroup.getVmAffinityRule().equals(other.getVmAffinityRule())
                && affinityGroup.getVdsAffinityRule().equals(other.getVdsAffinityRule());
    }
}
