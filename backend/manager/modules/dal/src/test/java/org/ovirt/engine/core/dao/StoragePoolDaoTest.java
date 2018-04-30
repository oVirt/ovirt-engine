package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class StoragePoolDaoTest extends BaseGenericDaoTestCase<Guid, StoragePool, StoragePoolDao> {
    private static final int NUMBER_OF_POOLS_FOR_PRIVELEGED_USER = 1;

    @Override
    protected StoragePool generateNewEntity() {
        StoragePool newPool = new StoragePool();
        newPool.setId(Guid.newGuid());
        newPool.setName("newPoolDude");
        newPool.setCompatibilityVersion(Version.getLast());
        return newPool;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setdescription("Farkle");
        existingEntity.setStoragePoolFormatType(StorageFormatType.V1);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.DATA_CENTER;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 8;
    }

    @Test
    public void testGetFilteredWithPermissions() {
        StoragePool result = dao.get(getExistingEntityId(), PRIVILEGED_USER_ID, true);

        assertGetResult(result);
    }

    @Test
    public void testGetFilteredWithPermissionsNoPermissions() {
        StoragePool result = dao.get(getExistingEntityId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    @Test
    public void testGetFilteredWithPermissionsNoPermissionsAndNoFilter() {
        StoragePool result = dao.get(getExistingEntityId(), UNPRIVILEGED_USER_ID, false);

        assertGetResult(result);
    }

    /**
     * Ensures an invalid name returns null.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        StoragePool result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures retrieving by name works as expected.
     */
    @Test
    public void testGetByName() {
        StoragePool result = dao.getByName(existingEntity.getName());

        assertGetResult(result);
    }

    /**
     * Asserts the result of {@link StoragePoolDao#get(Guid, Guid, boolean)} is correct
     * @param result The result to check
     */
    private void assertGetResult(StoragePool result) {
        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    /**
     * Ensures the right pool is retrieves for the given VDS.
     */
    @Test
    public void testGetForVds() {
        StoragePool result = dao.getForVds(FixturesTool.VDS_RHEL6_NFS_SPM);

        assertNotNull(result);
    }

    /**
     * Ensures the right pool is returned.
     */
    @Test
    public void testGetForCluster() {
        StoragePool result = dao.getForCluster(FixturesTool.CLUSTER);

        assertNotNull(result);
    }

    @Test
    public void testGetAllByStatus() {
        List<StoragePool> result = dao.getAllByStatus(StoragePoolStatus.Up);
        assertNotNull(result, "list of returned pools in status up shouldn't be null");
        assertEquals(7, result.size(), "wrong number of storage pools returned for up status");

        result = dao.getAllByStatus(StoragePoolStatus.Maintenance);
        assertNotNull(result, "list of returned pools in status up shouldn't be null");
        assertEquals(0, result.size(), "wrong number of storage pool returned for maintenance status");
    }

    /**
     * Ensures that retrieving storage pools works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<StoragePool> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_POOLS_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingEntity);
    }

    /**
     * Ensures that retrieving storage pools works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<StoragePool> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no storage pool retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetAllWithPermissionsUnprivilegedUser() {
        List<StoragePool> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all storage pools for the given domain are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<StoragePool> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that multiple data centers are returned if an external network had been imported to more than one.
     */
    @Test
    public void testDataCentersByExternalNetworkId() {
        List<Guid> result = dao.getDcIdByExternalNetworkId(FixturesTool.EXTERNAL_NETWORK_ID);

        assertNotNull(result);
        assertTrue(result.size() > 1);
        assertTrue(!result.get(0).equals(result.get(1)));
    }

    /**
     * Ensures that no data centers are returned for an external network that hadn't been imported.
     */
    @Test
    public void testNoDataCentersByExternalNetworkId() {
        List<Guid> result = dao.getDcIdByExternalNetworkId("foo");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that partial updating a storage pool works as expected.
     */
    @Test
    public void testPartialUpdate() {
        existingEntity.setdescription("NewFarkle");
        existingEntity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);

        dao.updatePartial(existingEntity);

        StoragePool result = dao.get(getExistingEntityId());

        assertGetResult(result);
    }

    /**
     * Ensures that updating a storage pool status works as expected.
     */
    @Test
    public void testUpdateStatus() {
        dao.updateStatus(getExistingEntityId(), StoragePoolStatus.NotOperational);
        existingEntity.setStatus(StoragePoolStatus.NotOperational);

        StoragePool result = dao.get(getExistingEntityId());

        assertGetResult(result);
    }

    @Test
    public void testIncreaseStoragePoolMasterVersion() {
        int result = dao.increaseStoragePoolMasterVersion(getExistingEntityId());
        StoragePool dbPool = dao.get(getExistingEntityId());
        assertEquals(result, existingEntity.getMasterDomainVersion() + 1);
        assertEquals(result, dbPool.getMasterDomainVersion());
    }

    /**
     * Asserts the result from {@link StoragePoolDao#getAll()} is correct without filtering
     *
     * @param result A list of storage pools to assert
     */
    private static void assertCorrectGetAllResult(List<StoragePool> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
