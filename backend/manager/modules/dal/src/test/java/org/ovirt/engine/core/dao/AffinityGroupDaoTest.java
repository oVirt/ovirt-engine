package org.ovirt.engine.core.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AffinityGroupDaoTest extends BaseDaoTestCase<AffinityGroupDao> {

    private static final String AFFINITY_GROUP_NAME = "affinityGroup1";
    private static final int NUM_OF_AFFINITY_GROUPS_IN_CLUSTER = 3;
    private static final int NUM_OF_AFFINITY_GROUPS_FOR_VM = 2;
    private static final int NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP = 2;

    @Inject
    private LabelDao labelDao;

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
    public void testSetAffinityGroupsForVm() {
        assertThat(dao.getAllAffinityGroupsByVmId(FixturesTool.VM_RHEL5_POOL_50))
                .extracting("id")
                .hasSize(2)
                .containsOnly(FixturesTool.EXISTING_AFFINITY_GROUP_ID, FixturesTool.AFFINITY_GROUP_2);

        dao.setAffinityGroupsForVm(FixturesTool.VM_RHEL5_POOL_50, Collections.singletonList(FixturesTool.AFFINITY_GROUP_3));

        assertThat(dao.getAllAffinityGroupsByVmId(FixturesTool.VM_RHEL5_POOL_50))
                .extracting("id")
                .hasSize(1)
                .containsOnly(FixturesTool.AFFINITY_GROUP_3);
    }

    @Test
    public void testSetAffinityGroupsForHost() {
        Guid hostId = FixturesTool.VDS_RHEL6_NFS_SPM;

        List<Guid> groups = dao.getAllAffinityGroupsByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI).stream()
                .filter(ag -> ag.getVdsIds().contains(hostId))
                .map(AffinityGroup::getId)
                .collect(Collectors.toList());

        assertThat(groups)
                .hasSize(1)
                .containsOnly(FixturesTool.EXISTING_AFFINITY_GROUP_ID);

        dao.setAffinityGroupsForHost(hostId, Arrays.asList(FixturesTool.AFFINITY_GROUP_2, FixturesTool.AFFINITY_GROUP_3));

        groups = dao.getAllAffinityGroupsByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI).stream()
                .filter(ag -> ag.getVdsIds().contains(hostId))
                .map(AffinityGroup::getId)
                .collect(Collectors.toList());

        assertThat(groups)
                .hasSize(2)
                .containsOnly(FixturesTool.AFFINITY_GROUP_2, FixturesTool.AFFINITY_GROUP_3);
    }

    @Test
    public void testGetWithFlatLabelsByClusterId() {
        Guid labelId1= Guid.newGuid();
        Guid labelId2 = Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .vm(FixturesTool.VM_RHEL5_POOL_57)
                .build());

        labelDao.save(new LabelBuilder()
                .id(labelId2)
                .name("label2")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .build());

        AffinityGroup group = dao.get(FixturesTool.AFFINITY_GROUP_3);
        group.setVmLabels(Arrays.asList(labelId1, labelId2));
        dao.update(group);

        List<AffinityGroup> groups = dao.getAllAffinityGroupsWithFlatLabelsByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);

        assertThat(groups).hasSize(3);

        group = groups.stream()
                .filter(ag -> ag.getId().equals(FixturesTool.AFFINITY_GROUP_3))
                .findFirst()
                .get();

        assertThat(group.getVmIds())
                .hasSize(2)
                .containsOnly(FixturesTool.VM_RHEL5_POOL_50, FixturesTool.VM_RHEL5_POOL_57);
    }

    @Test
    public void testGetWithFlatLabelsByVmId() {
        Guid labelId1= Guid.newGuid();
        Guid labelId2 = Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .vm(FixturesTool.VM_RHEL5_POOL_57)
                .build());

        labelDao.save(new LabelBuilder()
                .id(labelId2)
                .name("label2")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .build());

        AffinityGroup group = dao.get(FixturesTool.AFFINITY_GROUP_3);
        group.setVmLabels(Arrays.asList(labelId1, labelId2));
        dao.update(group);

        List<AffinityGroup> groups = dao.getAllAffinityGroupsWithFlatLabelsByVmId(FixturesTool.VM_RHEL5_POOL_50);

        assertThat(groups).hasSize(3);

        group = groups.stream()
                .filter(ag -> ag.getId().equals(FixturesTool.AFFINITY_GROUP_3))
                .findFirst()
                .get();

        assertThat(group.getVmIds())
                .hasSize(2)
                .containsOnly(FixturesTool.VM_RHEL5_POOL_50, FixturesTool.VM_RHEL5_POOL_57);
    }

    @Test
    public void testAddVmForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP, existing.getVmEntityNames().size());
        dao.insertAffinityVm(existing.getId(), FixturesTool.VM_RHEL5_POOL_51);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(NUM_OF_VMS_IN_EXISTING_AFFINITY_GROUP + 1, fetched.getVmEntityNames().size());
    }

    @Test
    public void testRemoveVmForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        dao.insertAffinityVm(existing.getId(), FixturesTool.VM_RHEL5_POOL_51);
        existing = dao.get(existing.getId());
        int numOfVms = existing.getVmEntityNames().size();
        dao.deleteAffinityVm(existing.getId(), FixturesTool.VM_RHEL5_POOL_51);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfVms - 1, fetched.getVmEntityNames().size());
    }

    @Test
    public void testAddHostForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        int numOfhosts = existing.getVdsEntityNames().size();
        dao.insertAffinityHost(existing.getId(), FixturesTool.VDS_RHEL6_NFS_SPM);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfhosts + 1, fetched.getVdsEntityNames().size());
    }

    @Test
    public void testRemoveHostForExistingAffinityGroup() {
        AffinityGroup existing = dao.get(FixturesTool.EXISTING_AFFINITY_GROUP_ID);
        int numOfhosts = existing.getVdsEntityNames().size();
        dao.deleteAffinityHost(existing.getId(), FixturesTool.VDS_RHEL6_NFS_SPM);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfhosts - 1, fetched.getVdsEntityNames().size());
    }

    @Test
    public void testAddVmLabelForExistingAffinityGroup() {
        Guid labelId1= Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .vm(FixturesTool.VM_RHEL5_POOL_57)
                .build());

        AffinityGroup existing = dao.get(FixturesTool.AFFINITY_GROUP_3);
        int numOfVmLabels = existing.getVmLabelNames().size();
        dao.insertAffinityVmLabel(existing.getId(), labelId1);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfVmLabels + 1, fetched.getVmLabelNames().size());
    }

    @Test
    public void testRemoveVmLabelForExistingAffinityGroup() {
        Guid labelId1= Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .vm(FixturesTool.VM_RHEL5_POOL_50)
                .vm(FixturesTool.VM_RHEL5_POOL_57)
                .build());

        AffinityGroup existing = dao.get(FixturesTool.AFFINITY_GROUP_3);
        dao.insertAffinityVmLabel(existing.getId(), labelId1);
        existing = dao.get(existing.getId());
        int numOfVmLabels = existing.getVmLabelNames().size();
        dao.deleteAffinityVmLabel(existing.getId(), labelId1);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfVmLabels - 1, fetched.getVmLabelNames().size());
    }

    @Test
    public void testAddHostLabelForExistingAffinityGroup() {
        Guid labelId1= Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .host(FixturesTool.VDS_RHEL6_NFS_SPM)
                .build());

        AffinityGroup existing = dao.get(FixturesTool.AFFINITY_GROUP_3);
        int numOfHostLabels = existing.getHostLabelNames().size();
        dao.insertAffinityHostLabel(existing.getId(), labelId1);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfHostLabels + 1, fetched.getHostLabelNames().size());
    }

    @Test
    public void testRemoveHostLabelForExistingAffinityGroup() {
        Guid labelId1= Guid.newGuid();

        labelDao.save(new LabelBuilder()
                .id(labelId1)
                .name("label1")
                .host(FixturesTool.VDS_RHEL6_NFS_SPM)
                .build());

        AffinityGroup existing = dao.get(FixturesTool.AFFINITY_GROUP_3);
        dao.insertAffinityHostLabel(existing.getId(), labelId1);
        existing = dao.get(existing.getId());
        int numOfHostLabels = existing.getHostLabelNames().size();
        dao.deleteAffinityHostLabel(existing.getId(), labelId1);
        AffinityGroup fetched = dao.get(existing.getId());
        assertEquals(numOfHostLabels - 1, fetched.getHostLabelNames().size());
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
