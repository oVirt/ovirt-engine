package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;

public class VdsGroupDAOTest extends BaseDAOTestCase {
    private static final int NUMBER_OF_GROUPS = 9;
    private static final int NUMBER_OF_TRUSTED_GROUPS = 4;
    private static final int NUMBER_OF_GROUPS_FOR_PRIVELEGED_USER = 2;

    private VdsGroupDAO dao;
    private VDS existingVds;
    private VDSGroup existingVdsGroup;
    private VDSGroup newGroup;
    private VDSGroup groupWithNoRunningVms;
    private StoragePool storagePool;
    private ClusterPolicyDao clusterPolicyDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        VdsDAO vdsDAO = dbFacade.getVdsDao();

        existingVds = vdsDAO.get(FixturesTool.VDS_RHEL6_NFS_SPM);

        StoragePoolDAO storagePoolDAO = dbFacade.getStoragePoolDao();

        storagePool = storagePoolDAO.get(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        dao = dbFacade.getVdsGroupDao();

        existingVdsGroup = dao.get(existingVds.getVdsGroupId());
        groupWithNoRunningVms = dbFacade.getVdsGroupDao().get(FixturesTool.VDS_GROUP_NO_RUNNING_VMS);

        newGroup = new VDSGroup();
        newGroup.setName("New VDS Group");
        newGroup.setcompatibility_version(new Version("3.0"));
        newGroup.setVirtService(true);
        newGroup.setGlusterService(false);
        newGroup.setClusterPolicyId(existingVdsGroup.getClusterPolicyId());
        clusterPolicyDao = dbFacade.getClusterPolicyDao();
        // set cluster policy name to allow equals method to succeed
        newGroup.setClusterPolicyName(clusterPolicyDao.get(existingVdsGroup.getClusterPolicyId()).getName());
        newGroup.setClusterPolicyProperties(new LinkedHashMap<String, String>());
        newGroup.setDetectEmulatedMachine(true);
        newGroup.setEmulatedMachine("rhel6.4.0");
        newGroup.setArchitecture(ArchitectureType.x86_64);

    }

    /**
     * Ensures that the id must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        VDSGroup result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that null is returned.
     */
    @Test
    public void testGetWithRunningVmsWhereThereAreNone() {
        VDSGroup result = dao.getWithRunningVms(groupWithNoRunningVms.getId());

        assertNull(result);
    }

    /**
     * Ensures that the VDS group is returned.
     */
    @Test
    public void testGetWithRunningVms() {
        VDSGroup result = dao.getWithRunningVms(existingVdsGroup.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that retrieving a group works as expected.
     */
    @Test
    public void testGet() {
        VDSGroup result = dao.get(existingVdsGroup.getId());

        assertCorrectVDSGroup(result);
    }

    /**
     * Ensures that retrieving a group works as expected with a privileged user and optional filtering.
     */
    @Test
    public void testGetFilteredWithPermissions() {
        VDSGroup result = dao.get(existingVdsGroup.getId(), PRIVILEGED_USER_ID, true);

        assertCorrectVDSGroup(result);
    }

    /**
     * Ensures that retrieving a group works as expected with an unprivileged user and optional filtering disabled.
     */
    @Test
    public void testGetFilteredWithNoPermissionsAndNoFilter() {
        VDSGroup result = dao.get(existingVdsGroup.getId(), UNPRIVILEGED_USER_ID, false);

        assertCorrectVDSGroup(result);
    }

    /**
     * Ensures that retrieving a group works as expected with an unprivileged user.
     */
    @Test
    public void testGetFilteredWithNoPermissions() {
        VDSGroup result = dao.get(existingVdsGroup.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Asserts that the given {@link VDSGroup} is indeed the existing VDS Group the test uses.
     *
     * @param group
     *            The group to check
     */
    private void assertCorrectVDSGroup(VDSGroup group) {
        assertNotNull(group);
        assertEquals(existingVdsGroup, group);
    }

    /**
     * Ensures that a bad name result in a null group.
     */
    @Test
    public void testGetByNameWithBadName() {
        VDSGroup result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetByName() {
        VDSGroup result = dao.getByName(existingVdsGroup.getName());

        assertCorrectVDSGroup(result);
    }

    @Test
    public void testGetByNameForPrivilegedUser() {
        VDSGroup result = dao.getByName(existingVdsGroup.getName(), PRIVILEGED_USER_ID, false);
        assertCorrectVDSGroup(result);
    }

    @Test
    public void testGetByNameForUnprivilegedUser() {
        VDSGroup result = dao.getByName(existingVdsGroup.getName(), UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<VDSGroup> result = dao.getAllForStoragePool(Guid.newGuid());
        assertGetAllForStoragePoolInvalidResult(result);
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId());
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that no groups are returned if the issuing user does not have the right permissions.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithNoPermissions() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId(), UNPRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolInvalidResult(result);
    }

    /**
     * Ensures that the right group is returned if the filtering mechanism is disabled.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithNoPermissionsAndNoFilter() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId(), UNPRIVILEGED_USER_ID, false);
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that no groups are returned if the issuing user has the right permissions.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithPermissions() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId(), PRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that the storage pool name is returned properly.
     */
    @Test
    public void testVdsGroupCorrectStoragePoolName() {
        List<VDSGroup> result = dao.getAllForStoragePool(storagePool.getId());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDSGroup group : result) {
            assertEquals(storagePool.getName(), group.getStoragePoolName());
        }
    }

    /**
     * Asserts the result of a invalid call to {@link VdsGroupDAO#getAllForStoragePool(Guid, Guid, boolean)}
     *
     * @param result
     */
    private static void assertGetAllForStoragePoolInvalidResult(List<VDSGroup> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of a valid call to {@link VdsGroupDAO#getAllForStoragePool(Guid, Guid, boolean)}
     *
     * @param result
     */
    private void assertGetAllForStoragePoolValidResult(List<VDSGroup> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDSGroup group : result) {
            assertEquals(storagePool.getId(), group.getStoragePoolId());
        }
    }

    /**
     * Ensures that the right number of groups are returned.
     */
    @Test
    public void testGetAll() {
        List<VDSGroup> result = dao.getAll();

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that retrieving VDS groups works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<VDSGroup> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_GROUPS_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingVdsGroup);
    }

    /**
     * Ensures that retrieving VDS groups works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<VDSGroup> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no VDS group is retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        List<VDSGroup> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures saving a group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newGroup);

        VDSGroup result = dao.getByName(newGroup.getName());

        assertNotNull(result);
        assertEquals(newGroup, result);
    }

    /**
     * Ensures that updating a group works as expected.
     */
    @Test
    public void testUpdate() {
        String oldName = existingVdsGroup.getName();

        existingVdsGroup.setName("This is the new name");
        existingVdsGroup.setVirtService(false);
        existingVdsGroup.setGlusterService(true);

        dao.update(existingVdsGroup);

        VDSGroup result = dao.get(existingVdsGroup.getId());

        assertCorrectVDSGroup(result);

        result = dao.getByName(oldName);

        assertNull(result);
    }

    /**
     * Ensures that removing a group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(groupWithNoRunningVms.getId());

        VDSGroup result = dao.get(groupWithNoRunningVms.getId());

        assertNull(result);
    }

    /**
     * Test the use of the special procedure to update emulated_machine
     */
    @Test
    public void testSetEmulatedMachine() {
        String preUpdate = existingVdsGroup.getEmulatedMachine();
        String updatedValue = "pc-version-1.2.3";

        assertNotSame(preUpdate, updatedValue);

        existingVdsGroup.setEmulatedMachine(updatedValue);
        dao.setEmulatedMachine(existingVdsGroup.getId(), updatedValue, false);

        assertEquals(updatedValue, dao.get(existingVdsGroup.getId()).getEmulatedMachine());
    }

    /**
     * Test the use of the special procedure to update detect_emulated_machine
     */
    @Test
    public void testSetDetectEmulatedMachine() {
        boolean preUpdate = existingVdsGroup.isDetectEmulatedMachine();
        boolean updateValue = false;

        assertNotSame(preUpdate, updateValue);

        dao.setEmulatedMachine(existingVdsGroup.getId(), existingVdsGroup.getEmulatedMachine(), updateValue);

        assertEquals(updateValue, dao.get(existingVdsGroup.getId()).isDetectEmulatedMachine());
    }

    @Test
    public void testUpdateVdsGroupsProps() {
        assertNotSame("pc-1.2.3", existingVdsGroup.getEmulatedMachine());
        existingVdsGroup.setEmulatedMachine("pc-1.2.3");
        dao.update(existingVdsGroup);
        assertEquals("pc-1.2.3", existingVdsGroup.getEmulatedMachine());

        existingVdsGroup.setDetectEmulatedMachine(true);
        dao.update(existingVdsGroup);
        assertEquals(true, existingVdsGroup.isDetectEmulatedMachine());
    }


    /**
     * Test that the correct vds_groups are fetched when looking for trusted_services
     */
    @Test
    public void testGetAllTrustedVdsGroups() {
        List<VDSGroup> trustedClusters = dao.getTrustedClusters();

        assertNotNull(trustedClusters);
        assertFalse(trustedClusters.isEmpty());
        assertEquals(trustedClusters.size(), NUMBER_OF_TRUSTED_GROUPS);
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.VDS_GROUP_RHEL6_NFS)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.VDS_GROUP_RHEL6_NFS_2)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.VDS_GROUP_RHEL6_LOCALFS)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.VDS_GROUP_RHEL6_NFS_NO_SPECIFIC_QUOTAS)));
    }

    /**
     * Test that the correct vds_group is fetched when querying by cluster policy id
     */
    @Test
    public void testGetClusterByClusterPolicyId() {
        List<VDSGroup> result = dao.getClustersByClusterPolicyId(FixturesTool.CLUSTER_POLICY_EVEN_DISTRIBUTION);
        List<Guid> vdsGroupIdList = new ArrayList<Guid>();
        for (VDSGroup group : result) {
            vdsGroupIdList.add(group.getId());
        }

        assertTrue(vdsGroupIdList.contains(FixturesTool.VDS_GROUP_RHEL6_ISCSI));
        assertTrue(vdsGroupIdList.contains(FixturesTool.VDS_GROUP_NO_RUNNING_VMS));
        assertTrue(vdsGroupIdList.contains(FixturesTool.VDS_GROUP_RHEL6_NFS));
    }

    /**
     * Test that no vds_group is fetched when querying by wrong cluster policy id
     */
    @Test
    public void testGetClusterByClusterPolicyIdNegative() {
        List<VDSGroup> result = dao.getClustersByClusterPolicyId(Guid.newGuid());

        assertTrue(result == null || result.size() == 0);
    }

    /**
     * Asserts the result from {@link VdsGroupDAO#getAll()} is correct without filtering
     *
     * @param result
     */
    private void assertCorrectGetAllResult(List<VDSGroup> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_GROUPS, result.size());
    }

    @Test
    public void testGetVmsCountByClusterId() {
        // Cluster with no VMs
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(FixturesTool.VDS_GROUP_RHEL6_NFS),
                FixturesTool.NUMBER_OF_VMS_IN_VDS_GROUP_RHEL6_NFS_CLUSTER);

        // Cluster with VMs
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(FixturesTool.VDS_GROUP_RHEL6_ISCSI),
                FixturesTool.NUMBER_OF_VMS_IN_VDS_GROUP_RHEL6_ISCSI);

        // Non existing cluster, should return 0
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(Guid.newGuid()), 0);
    }

    @Test
    public void testGetVmHostCount() throws Exception {
        Guid guid = FixturesTool.VDS_GROUP_RHEL6_ISCSI;
        List<VDSGroup> vdsGroups = new ArrayList<>();
        vdsGroups.add(dao.get(guid));
        List<VDSGroup> data = ((VdsGroupDAODbFacadeImpl) dao).getHostsAndVmsForClusters(vdsGroups);
        assertEquals("Incorrect number of VMs in cluster", data.get(0).getGroupHostsAndVms().getVms(), 7);
        assertEquals("Incorrect number of Hosts in cluster", data.get(0).getGroupHostsAndVms().getHosts(), 1);
    }

    @Test
    public void testGetClustersByServiceAndCompatibilityVersion() {
        List<VDSGroup> vdsGroups = ((VdsGroupDAODbFacadeImpl)dao).getClustersByServiceAndCompatibilityVersion(true, false, "2.3");
        assertNotNull(vdsGroups);
        assertEquals(1, vdsGroups.size());
        assertEquals(FixturesTool.GLUSTER_CLUSTER_ID, vdsGroups.get(0).getId());
    }
}
