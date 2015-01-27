package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.compat.Guid;

public class VdsDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");
    private static final Guid EXISTING_VDS_ID_2 = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");

    private static final Guid VDS_GROUP_WITH_FEDORA = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private static final Guid VDS_GROUP_WITH_RHELS = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d2");

    private static final String IP_ADDRESS = "192.168.122.17";
    private VdsDAO dao;
    private VDS existingVds;
    private VDS existingVds2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVdsDao();
        existingVds = dao.get(EXISTING_VDS_ID);
        existingVds2 = dao.get(EXISTING_VDS_ID_2);
    }

    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VDS result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected.
     */
    @Test
    public void testGet() {
        VDS result = dao.get(existingVds.getId());

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected for a privileged user.
     */
    @Test
    public void testGetWithPermissionsPrivilegedUser() {
        VDS result = dao.get(existingVds.getId(), PRIVILEGED_USER_ID, true);

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsDisabledUnprivilegedUser() {
        VDS result = dao.get(existingVds.getId(), UNPRIVILEGED_USER_ID, false);

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that no VDS is retrieved for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        VDS result = dao.get(existingVds.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Ensures that {@code null} is returned.
     */
    @Test
    public void testGetUsingInvalidName() {
        VDS result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Asserts the result from {@link VdsDAO#get(Guid)} is correct.
     *
     * @param result
     */
    private void assertCorrectGetResult(VDS result) {
        assertNotNull(result);
        assertEquals(existingVds, result);
    }

    /**
     * Asserts the result from a call to {@link VdsDAO#get(Guid)}
     * that isn't supposed to return any data is indeed empty.
     *
     * @param result
     */
    private static void assertIncorrectGetResult(List<VDS> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of objects are returned with the given name.
     */
    @Test
    public void testGetWithName() {
        VDS result = dao.getByName(existingVds.getName());

        assertNotNull(result);
        assertEquals(existingVds.getName(), result.getName());
    }

    /**
     * Ensures that the right set of VDS instances are returned for the given hostname.
     */
    @Test
    public void testGetAllForHostname() {
        List<VDS> result = dao.getAllForHostname(existingVds.getHostName());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getHostName(), vds.getHostName());
        }
    }

    /**
     * Ensures the right set of VDS instances are returned.
     */
    @Test
    public void testGetAllWithUniqueId() {
        List<VDS> result = dao.getAllWithUniqueId(existingVds.getUniqueId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getUniqueId(), vds.getUniqueId());
        }
    }

    /**
     * Ensures that an empty collection is returned if the type is not present.
     */
    @Test
    public void testGetAllOfTypeWithUnrepresentedType() {
        List<VDS> result = dao.getAllOfType(VDSType.oVirtNode);

        assertIncorrectGetResult(result);
    }

    /**
     * Ensures that all of the right instances for the given type.
     */
    @Test
    public void testGetAllOfType() {
        List<VDS> result = dao.getAllOfType(VDSType.VDS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(VDSType.VDS, vds.getVdsType());
        }
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllOfTypesWithUnrepresentedTypes() {
        List<VDS> result = dao
                .getAllOfTypes(new VDSType[] { VDSType.oVirtNode });

        assertIncorrectGetResult(result);
    }

    /**
     * Ensures that all of the right instances for the given types.
     */
    @Test
    public void testGetAllOfTypes() {
        List<VDS> result = dao.getAllOfTypes(new VDSType[] { VDSType.VDS });

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(VDSType.VDS, vds.getVdsType());
        }
    }

    /**
     * Ensures the API works as expected.
     */
    @Test
    public void testGetAllForVdsGroupWithoutMigrating() {
        List<VDS> result = dao.getAllForVdsGroupWithoutMigrating(existingVds
                .getVdsGroupId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getVdsGroupId(), vds.getVdsGroupId());
        }
    }

    /**
     * Ensures that all VDS instances are returned.
     */
    @Test
    public void testGetAll() {
        List<VDS> result = dao.getAll();

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that retrieving VDS works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<VDS> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(existingVds));
    }

    /**
     * Ensures that retrieving VDS works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<VDS> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no VDS is retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetAllWithPermissionsUnprivilegedUser() {
        List<VDS> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all VDS related to the VDS group supplied.
     */
    @Test
    public void testGetAllForVdsGroup() {
        List<VDS> result = dao.getAllForVdsGroup(existingVds.getVdsGroupId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getVdsGroupId(), vds.getVdsGroupId());
        }
    }

    /**
     * Ensures that the VDS instances are returned according to spm priority
     */
    @Test
    public void testGetListForSpmSelection() {
        final Guid STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
        List<VDS> result = dao.getListForSpmSelection(STORAGE_POOL_ID);
        assertTrue(result.get(0).getVdsSpmPriority() >= result.get(1).getVdsSpmPriority());
    }

    /**
     * Asserts that the right collection containing the existing host is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllForVdsGroupWithPermissionsForPriviligedUser() {
        List<VDS> result = dao.getAllForVdsGroup(existingVds.getVdsGroupId(), PRIVILEGED_USER_ID, true);
        assertGetAllForVdsGroupCorrectResult(result);
    }

    /**
     * Asserts that an empty collection is returned for an non privileged user with filtering enabled
     */
    @Test
    public void testGetAllForVdsGroupWithPermissionsForUnpriviligedUser() {
        List<VDS> result = dao.getAllForVdsGroup(existingVds.getVdsGroupId(), UNPRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing the existing host is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllForVdsGroupWithPermissionsDisabledForUnpriviligedUser() {
        List<VDS> result = dao.getAllForVdsGroup(existingVds.getVdsGroupId(), UNPRIVILEGED_USER_ID, false);
        assertGetAllForVdsGroupCorrectResult(result);
    }

    @Test
    public void testGetAllForStoragePoolAndStatus() {
        assertNotNull(existingVds.getStatus());
        List<VDS> result = dao.getAllForStoragePoolAndStatus(existingVds.getStoragePoolId(), existingVds.getStatus());
        assertCorrectGetAllResult(result);
    }

    @Test
    public void testGetAllForStoragePoolAndStatusForAllStatuses() {
        dbFacade.getVdsDynamicDao().updateStatus(existingVds.getId(), VDSStatus.Maintenance);
        List<VDS> result = dao.getAllForStoragePoolAndStatus(existingVds.getStoragePoolId(), null);
        EnumSet<VDSStatus> statuses = EnumSet.noneOf(VDSStatus.class);
        for (VDS vds : result) {
            statuses.add(vds.getStatus());
        }
        assertCorrectGetAllResult(result);
        assertTrue("more then one different status expected", statuses.size() > 1);
    }

    private void assertGetAllForVdsGroupCorrectResult(List<VDS> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.iterator().next(), existingVds);

        for (VDS vds : result) {
            assertEquals(vds.getVdsGroupId(), existingVds.getVdsGroupId());
        }
    }

    /**
     * Asserts that the right collection of hosts is returned for a storage pool with hosts
     */
    @Test
    public void testGetAllForStoragePool() {
        List<VDS> result = dao.getAllForStoragePool(existingVds.getStoragePoolId());
        assertGetAllForStoragePoolCorrectResult(result);
    }

    /**
     * Asserts that an empty collection of hosts is returned for a storage pool with no hosts
     */
    @Test
    public void testGetAllForStoragePoolNoVds() {
        List<VDS> result = dao.getAllForStoragePool(Guid.newGuid());
        assertIncorrectGetResult(result);
    }

    /**
     * Asserts that the right collection of hosts is returned for a storage pool with hosts,
     * with a privileged user
     */
    @Test
    public void testGetAllForStoragePoolWithPermissions() {
        List<VDS> result = dao.getAllForStoragePool(existingVds.getStoragePoolId(), PRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolCorrectResult(result);
    }

    /**
     * Asserts that the right collection of hosts is returned for a storage pool with hosts,
     * with an unprivileged user, but with the permissions mechanism disabled
     */
    @Test
    public void testGetAllForStoragePoolWithNoPermissionsFilteringDisabled() {
        List<VDS> result = dao.getAllForStoragePool(existingVds.getStoragePoolId(), UNPRIVILEGED_USER_ID, false);
        assertGetAllForStoragePoolCorrectResult(result);
    }

    /**
     * Asserts that an empty collection of hosts is returned for a storage pool with hosts,
     * with an unprivileged user
     */
    @Test
    public void testGetAllForStoragePoolWithNoPermissions() {
        List<VDS> result = dao.getAllForStoragePool(existingVds.getStoragePoolId(), UNPRIVILEGED_USER_ID, true);
        assertIncorrectGetResult(result);
    }

    /**
     * Ensures that only the correct vds is fetched.
     */
    @Test
    public void testGetAllForNetwork() {
        List<VDS> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE_2);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingVds, result.get(0));
    }

    /**
     * Ensures that no vds is fetched since the network is not assigned to any cluster
     */
    @Test
    public void testGetAllForNetworkEmpty() {
        List<VDS> result = dao.getAllForNetwork(FixturesTool.NETWORK_NO_CLUSTERS_ATTACHED);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the vds with a network with the same name is not fetched.
     */
    @Test
    public void testGetAllForNetworkSameNetworkName() {
        List<VDS> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);
        assertNotNull(result);
        assertFalse(result.contains(existingVds));
    }

    /**
     * Ensures that only the correct vds is fetched.
     */
    @Test
    public void testGetAllWithoutNetwork() {
        List<VDS> result = dao.getAllWithoutNetwork(FixturesTool.NETWORK_ENGINE_2);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingVds2, result.get(0));
    }

    @Test
    public void testGetHostsForStorageOperation() {
        List<VDS> result = dao.getHostsForStorageOperation(null, false);
        assertNotNull(result);
        assertGetHostsForStorageOperationNonGluster(result);
    }

    private void assertGetHostsForStorageOperationNonGluster(List<VDS> result) {
        for (VDS vds : result) {
            assertThat(vds.getVdsGroupId(), not(equalTo(FixturesTool.GLUSTER_CLUSTER_ID)));
        }
    }

    @Test
    public void testGetHostsForStorageOperationByStoragePool() {
        List<VDS> result = dao.getHostsForStorageOperation(FixturesTool.STORAGE_POOL_RHEL6_ISCSI, false);
        assertNotNull(result);
        assertGetHostsForStorageOperationCorrectStoragePool(result);
    }

    private void assertGetHostsForStorageOperationCorrectStoragePool(List<VDS> result) {
        for (VDS vds : result) {
            assertEquals(vds.getStoragePoolId(), FixturesTool.STORAGE_POOL_RHEL6_ISCSI);
        }
    }

    @Test
    public void testGetHostsForStorageOperationLocalFsOnly() {
        List<VDS> result = dao.getHostsForStorageOperation(null, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FixturesTool.VDS_GROUP_RHEL6_LOCALFS, result.get(0).getVdsGroupId());
    }

    public void testGetFirstUpRhelForVdsGroupFromClusterWithRhels() {
        VDS vds = dao.getFirstUpRhelForVdsGroup(VDS_GROUP_WITH_RHELS);
        assertNotNull(vds);
    }

    public void testGetFirstUpRhelForVdsGroupFromClusterWithFedoras() {
        VDS vds = dao.getFirstUpRhelForVdsGroup(VDS_GROUP_WITH_FEDORA);
        assertNull(vds);
    }

    private void assertGetAllForStoragePoolCorrectResult(List<VDS> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (VDS vds : result) {
            assertEquals("Wrong storage pool for VDS", existingVds.getStoragePoolId(), vds.getStoragePoolId());
        }
    }

    private void assertCorrectGetAllResult(List<VDS> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
