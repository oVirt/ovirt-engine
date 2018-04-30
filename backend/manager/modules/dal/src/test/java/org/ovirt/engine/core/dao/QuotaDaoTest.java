package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.compat.Guid;

public class QuotaDaoTest extends BaseDaoTestCase<QuotaDao> {
    private static final Long unlimited = -1L;
    private static final int STORAGE_NUM_QUOTAS = 5;
    private static final int VDS_GRUOP_NUM_QUOTAS = 4;

    @Test
    public void testGeneralQuotaLimitations() {
        // Set new Quota definition.
        Quota quota = createGeneralQuota();
        setQuotaGlobalLimitations(quota);
        quota.setQuotaClusters(getQuotaCluster(null));
        quota.setQuotaStorages(getQuotaStorage(null));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());

        assertNotNull(quotaEntity);
        assertEquals(quota, quotaEntity);
        assertEquals("rhel6.NFS", quotaEntity.getStoragePoolName());
        assertEquals(QuotaEnforcementTypeEnum.DISABLED, quotaEntity.getQuotaEnforcementType());
    }

    @Test
    public void testSpecificQuotaLimitations() {
        // Set new Quota definition.
        Quota quota = createGeneralQuota();
        quota.setQuotaClusters(getQuotaCluster(getSpecificQuotaCluster(quota.getId())));
        quota.setQuotaStorages(getQuotaStorage(getSpecificQuotaStorage(quota.getId())));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quota, quotaEntity);
    }

    @Test
    public void testSpecificAndGeneralQuotaLimitations() {
        // Set new Quota definition.
        Quota quota = createGeneralQuota();
        quota.setQuotaClusters(getQuotaCluster(getSpecificQuotaCluster(quota.getId())));
        quota.setQuotaStorages(getQuotaStorage(null));
        quota.setGlobalQuotaStorage(new QuotaStorage(null, null, null, 10000L, 0d));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quota, quotaEntity);
    }

    /**
     * Test scenario when there is a specific limitation on a vds group, and we check if there is enough resources on it.<BR/>
     * The returned value from the query, should be the specific limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchClusterWithUnlimitedGlobalLimitation() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);

        // Should be two rows of specific vds group
        assertEquals(1, quotaClusterList.size());

        // Get first specific vds group.
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(unlimited, quotaCluster.getMemSizeMB());
        assertEquals(10, (int) quotaCluster.getVirtualCpu());
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the global limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchGlobalQuotaUsageForSpecificCluster() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(1, quotaClusterList.size());
        assertTrue(quotaCluster.getMemSizeMBUsage() > 0);
        assertTrue(quotaCluster.getVirtualCpuUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(Integer.valueOf(100), quotaCluster.getVirtualCpu());
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the global limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchGlobalQuotaUsageForGlobalCluster() {
        List<QuotaCluster> quotaClusterList = dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_GENERAL);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertEquals(1, quotaClusterList.size());
        assertNotNull(quotaCluster);
        assertTrue(quotaCluster.getMemSizeMBUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(Integer.valueOf(100), quotaCluster.getVirtualCpu());
    }

    /**
     * Comparing the two previous test results.<BR/>
     * Since there is only global limitation for specific quota that is being checked, the results for fetching per vds
     * group or fetching per storage pool should be the same.
     */
    @Test
    public void testCompareFetchGlobalQuotaForSpecificAndForGlobalCluster() {
        List<QuotaCluster> quotaClusterGlobalList =
                dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_GENERAL);
        List<QuotaCluster> quotaClusterSpecificList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);

        // Check if the global variable returns when null is initialization.
        assertEquals(quotaClusterGlobalList, quotaClusterSpecificList);
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the specific limitation and the specific usage on the vds group.
     */
    @Test
    public void testFetchSpecificQuotaUsageForSpecificCluster() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(1, quotaClusterList.size());

        // Check if the global variable returns when null is initialization.
        assertEquals(Integer.valueOf(10), quotaCluster.getVirtualCpu());
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the specific limitations and the specific usage on the storage pool.
     */
    @Test
    public void testFetchSpecificQuotaUsageForGlobalCluster() {
        List<QuotaCluster> quotaClusterList = dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_SPECIFIC);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertEquals(2, quotaClusterList.size());
        assertNotNull(quotaCluster);
    }

    @Test
    public void testFetchSpecificAndGeneralQuotaForStorage() {
        List<QuotaStorage> quotaStorageList =
                dao.getQuotaStorageByStorageGuid(null, FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        QuotaStorage quotaStorage = quotaStorageList.get(0);
        assertEquals(1, quotaStorageList.size());
        assertNotNull(quotaStorage);

        // Check if the global variable returns when null is initialization.
        assertTrue(quotaStorage.getStorageSizeGBUsage() > 0);
    }

    @Test
    public void testFetchAllClusterForQuota() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(quotaClusterList);
        assertEquals(2, quotaClusterList.size());
        for (QuotaCluster quotaCluster : quotaClusterList) {
            if (quotaCluster.getQuotaClusterId()
                    .equals(new Guid("68c96e11-0aad-4e3a-9091-12897b7f2388"))) {
                assertEquals(Integer.valueOf(10), quotaCluster.getVirtualCpu());
                assertEquals(unlimited, quotaCluster.getMemSizeMB());
            } else if (quotaCluster.getQuotaClusterId()
                    .equals(new Guid("68c96e11-0aad-4e3a-9091-12897b7f2389"))) {
                assertEquals(Integer.valueOf(1000), quotaCluster.getVirtualCpu());
                assertEquals(unlimited, quotaCluster.getMemSizeMB());
            }
        }
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaClusterByQuotaGuidWithGeneralDefault(Guid)} is called
     * with a specific quota, all the relevant VDSs are returned
     */
    @Test
    public void testQuotaClusterByQuotaGuidWithGeneralDefaultNoDefault() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByQuotaGuidWithGeneralDefault(FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(quotaClusterList);
        assertEquals(2, quotaClusterList.size(), "wrong number of quotas returned");
        for (QuotaCluster group : quotaClusterList) {
            assertNotNull(group.getClusterId(), "VDS ID should not be null in specific mode");
            assertNotNull(group.getClusterName(), "VDS name should not be null in specific mode");
        }
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaClusterByQuotaGuidWithGeneralDefault(Guid)} is called
     * with a non-specific quota, the general is returned
     */
    @Test
    public void testQuotaClusterByQuotaGuidWithGeneralDefaultWithDefault() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByQuotaGuidWithGeneralDefault(FixturesTool.QUOTA_GENERAL);
        assertNotNull(quotaClusterList);
        assertEquals(1, quotaClusterList.size(), "wrong number of quotas returned");
        for (QuotaCluster group : quotaClusterList) {
            assertEquals(Guid.Empty, group.getClusterId(), "VDS ID should be empty in general mode");
            assertNull(group.getClusterName(), "VDS name should be null in general mode");
        }
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaClusterByQuotaGuidWithGeneralDefault(Guid)} is called
     * with an empty quota, no vds quotas are returned
     */
    @Test
    public void testQuotaClusterByQuotaGuidWithGeneralDefaultWithEmpty() {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByQuotaGuidWithGeneralDefault(FixturesTool.QUOTA_EMPTY);
        assertNotNull(quotaClusterList);
        assertEquals(0, quotaClusterList.size(), "wrong number of quotas returned");
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaStorageByQuotaGuidWithGeneralDefault(Guid)} is called
     * with a specific quota, all the relevant storages are returned
     */
    @Test
    public void testQuotaStorageByQuotaGuidWithGeneralDefaultNoDefault() {
        List<QuotaStorage> quotaStorageList =
                dao.getQuotaStorageByQuotaGuidWithGeneralDefault(FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(quotaStorageList);
        assertEquals(1, quotaStorageList.size(), "wrong number of quotas returned");
        for (QuotaStorage group : quotaStorageList) {
            assertNotNull(group.getStorageId(), "Storage ID should not be null in specific mode");
            assertNotNull(group.getStorageName(), "Storage name should not be null in specific mode");
        }
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaStorageByQuotaGuidWithGeneralDefault(Guid)} is called
     * with an empty quota, no storage quotas are returned
     */
    @Test
    public void testQuotaStorageByQuotaGuidWitheGeneralDefaultWithEmpty() {
        List<QuotaStorage> quotaStorageList =
                dao.getQuotaStorageByQuotaGuidWithGeneralDefault(FixturesTool.QUOTA_EMPTY);
        assertNotNull(quotaStorageList);
        assertEquals(0, quotaStorageList.size(), "wrong number of quotas returned");
    }

    /**
     * Asserts that when {@link QuotaDao#getQuotaStorageByQuotaGuidWithGeneralDefault(Guid)} is called
     * with a specific quota, all the relevant VDSs are returned
     */
    @Test
    public void testQuotaStorageByQuotaGuidWithGeneralDefaultWithDefault() {
        List<QuotaStorage> quotaStorageList =
                dao.getQuotaStorageByQuotaGuidWithGeneralDefault(FixturesTool.DEFAULT_QUOTA_GENERAL);
        assertNotNull(quotaStorageList);
        assertEquals(1, quotaStorageList.size(), "wrong number of quotas returned");
        for (QuotaStorage group : quotaStorageList) {
            assertEquals(Guid.Empty, group.getStorageId(), "Storage ID should not be null in general mode");
            assertNull(group.getStorageName(), "Storage name should not be null in general mode");
        }
    }

    @Test
    public void testRemoveQuota() {
        Quota quota = dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertNotNull(quota);
        dao.remove(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertNull(dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL));
        assertEquals(0, dao.getQuotaClusterByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
        assertEquals(0, dao.getQuotaStorageByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
    }

    /**
     * Make Quota specific to be the same as Quota general and specific.
     */
    @Test
    public void testUpdateQuota() {
        Quota quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Save quotaName and cluster list for future check.
        String quotaName = "New Temporary name";
        List<QuotaCluster> quotaClusterList =
                getQuotaCluster(getSpecificQuotaCluster(quotaGeneralToSpecific.getId()));
        Long newStorageLimit = 2345L;

        // Check before the update, that the fields are not equal.
        assertNotEquals(quotaName, quotaGeneralToSpecific.getQuotaName());
        assertNotEquals(quotaClusterList.size(), quotaGeneralToSpecific.getQuotaClusters().size());
        assertNotEquals(newStorageLimit, quotaGeneralToSpecific.getGlobalQuotaStorage().getStorageSizeGB());

        // Update
        quotaGeneralToSpecific.setQuotaName(quotaName);
        quotaGeneralToSpecific.getGlobalQuotaStorage().setStorageSizeGB(newStorageLimit);
        quotaGeneralToSpecific.setQuotaClusters(quotaClusterList);

        dao.update(quotaGeneralToSpecific);
        quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Check after the update, that the fields are equal now.
        assertEquals(quotaName, quotaGeneralToSpecific.getQuotaName());
        assertEquals(quotaClusterList.size(), quotaGeneralToSpecific.getQuotaClusters().size());
        assertEquals(newStorageLimit, quotaGeneralToSpecific.getGlobalQuotaStorage().getStorageSizeGB());
    }

    /**
     * Test get Quota by Name, with name of specific Quota.
     */
    @Test
    public void testGetQuotaByExistingName() {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General", FixturesTool.STORAGE_POOL_NFS);
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL)
                .getQuotaName(), quotaGeneralToSpecific.getQuotaName());
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL).getId(), quotaGeneralToSpecific.getId());
    }

    /**
     * Test get Quota by Name, with name of specific Quota.
     */
    @Test
    public void testGetQuotaByAdElementId() {
        List<Quota> quotaByAdElementIdList =
                dao.getQuotaByAdElementId(FixturesTool.USER_EXISTING_ID, FixturesTool.STORAGE_POOL_NFS, false);

        // Check if quota general has been fetched.
        assertEquals("Quota General", quotaByAdElementIdList.get(0).getQuotaName());
    }

    /**
     * Test get all Quotas in the setup
     */
    @Test
    public void testGetFetchAllQuotaInTheSetup() {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(null);
        assertEquals(13, quotaList.size());
    }

    /**
     * Test get Quota by storage pool Id
     */
    @Test
    public void testGetFetchForSpecificStoragePool() {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(FixturesTool.STORAGE_POOL_NFS);
        assertEquals(6, quotaList.size());
    }

    /**
     * Test get Quota by storage pool Id, for storage pool with no quotas in it.
     */
    @Test
    public void testFetchStoragePoolWithNoQuota() {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(Guid.newGuid());
        assertEquals(0, quotaList.size());
    }

    /**
     * Test get Quota by Name, with name that does not exist for the storage pool.
     */
    @Test
    public void testGetQuotaByExistingNameWIthNoMatchingStoragePool() {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General",
                FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        assertNull(quotaGeneralToSpecific);
    }

    @Test
    public void testGetDefaultQuotaForStoragePool() {
        Quota quota = dao.getDefaultQuotaForStoragePool(FixturesTool.STORAGE_POOL_NFS);
        assertNotNull(quota);
        assertEquals(FixturesTool.STORAGE_POOL_NFS, quota.getStoragePoolId());
        assertTrue(quota.isDefault());
    }

    /**
     * Test get Quota by Name, with not existing name.
     */
    @Test
    public void testGetQuotaWithNoExistingName() {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Any name", FixturesTool.STORAGE_POOL_NFS);
        assertNull(quotaGeneralToSpecific);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid, long, boolean)} with an existing storage domain
     */
    @Test
    public void testGetRelevantQuotasExistingStorage() {
        // there is one specific quota and all the general ones defined on this storage domain
        assertGetAllRelevantQuoatsForStorage(FixturesTool.STORAGE_DOMAIN_NFS_MASTER, STORAGE_NUM_QUOTAS);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid, long, boolean)}} with a storage domain with no specific quotas
     */
    @Test
    public void testGetRelevantQuotasExistingStorageNoSpecificQuotas() {
        // there are no specific quotas, but all the general quotas relate to the storage pool containing this domain
        assertGetAllRelevantQuoatsForStorage(FixturesTool.STORAGE_DOMAIN_NFS_ISO, STORAGE_NUM_QUOTAS - 1);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid, long, boolean)}} with a non existing storage domain
     */
    @Test
    public void testGetRelevantQuotasNonExistingStorage() {
        // There is no such storgae, so no quotas are defined on it
        assertGetAllRelevantQuoatsForStorage(Guid.newGuid(), 0);
    }

    /**
     * Asserts that {@code expectedQuotas} are relevant for the given {@code storageId}
     */
    private void assertGetAllRelevantQuoatsForStorage(Guid storageId, int expectedQuotas) {
        List<Quota> quotas = dao.getAllRelevantQuotasForStorage(storageId, FixturesTool.PRIVILEGED_SESSION_ID, false);
        assertEquals(expectedQuotas, quotas.size(), "Wrong number of quotas retuend");
    }

    /**
     * Test getAllRelevantQuotasForStorage(Guid, long, boolean)} with an existing VDS Group
     */
    @Test
    public void testGetRelevantQuotasExistingCluster() {
        // there is one specific quota and all the general ones defined on this VDS Group
        assertGetAllRelevantQuoatsForCluster(FixturesTool.CLUSTER_RHEL6_NFS, VDS_GRUOP_NUM_QUOTAS);
    }

    /**
     * Test getAllRelevantQuotasForStorage(Guid, long, boolean)} with a VDS Group domain with no specific quotas
     */
    @Test
    public void testGetRelevantQuotasExistingClusterNoSpecificQuotas() {
        // there are no specific quotas, but all the general quotas relate to the storage pool containing this group
        assertGetAllRelevantQuoatsForCluster(FixturesTool.CLUSTER_RHEL6_NFS_NO_SPECIFIC_QUOTAS, VDS_GRUOP_NUM_QUOTAS - 1);
    }

    /**
     * Test getAllRelevantQuotasForStorage(Guid, long, boolean)} with a non existing VDS Group
     */
    @Test
    public void testGetRelevantQuotasNonExistingCluster() {
        // There is no such storgae, so no quotas are defined on it
        assertGetAllRelevantQuoatsForCluster(Guid.newGuid(), 0);
    }

    /**
     * Test getAllRelevantQuotasForStorage(Guid, long, boolean)} fetching quota for user
     * without privileges for quota.
     */
    @Test
    public void testGetRelevantStorageQuotaForUserWithoutPrivileges() {
        List<Quota> quotas = dao.getAllRelevantQuotasForStorage(FixturesTool.STORAGE_DOMAIN_NFS_MASTER, FixturesTool.UNPRIVILEGED_SESSION_ID, true);
        assertEquals(0, quotas.size(), "Unprivileged user is not allowed to fetch for quota");
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForCluster(Guid, long, boolean)} fetching quota for user
     * without privileges for quota.
     */
    @Test
    public void testGetRelevantClusterQuotaForUserWithoutPrivileges() {
        List<Quota> quotas = dao.getAllRelevantQuotasForCluster(FixturesTool.CLUSTER_RHEL6_NFS,
                FixturesTool.UNPRIVILEGED_SESSION_ID,
                true);
        assertEquals(0, quotas.size(), "Unprivileged user is not allowed to fetch for quota");
    }

    /**
     * Asserts that {@code expectedQuotas} are relevant for the given {@code clusterId}
     */
    private void assertGetAllRelevantQuoatsForCluster(Guid clusterId, int expectedQuotas) {
        List<Quota> quotas = dao.getAllRelevantQuotasForCluster(clusterId, FixturesTool.PRIVILEGED_SESSION_ID, false);
        assertEquals(expectedQuotas, quotas.size(), "Wrong number of quotas retuend");
    }

    private static QuotaStorage getSpecificQuotaStorage(Guid quotaId) {
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setQuotaId(quotaId);
        quotaStorage.setQuotaStorageId(Guid.newGuid());
        quotaStorage.setStorageId(FixturesTool.STORAGE_DOMAIN_NFS_MASTER);
        quotaStorage.setStorageSizeGB(10000L);
        quotaStorage.setStorageSizeGBUsage(0d);
        return quotaStorage;

    }

    private static QuotaCluster getSpecificQuotaCluster(Guid quotaId) {
        QuotaCluster quotaCluster = new QuotaCluster();
        quotaCluster.setQuotaClusterId(Guid.newGuid());
        quotaCluster.setQuotaId(quotaId);
        quotaCluster.setClusterId(FixturesTool.CLUSTER_RHEL6_NFS);
        quotaCluster.setVirtualCpu(2880);
        quotaCluster.setMemSizeMB(16000000L);
        quotaCluster.setVirtualCpuUsage(0);
        quotaCluster.setMemSizeMBUsage(0L);
        return quotaCluster;
    }

    private static List<QuotaCluster> getQuotaCluster(QuotaCluster quotaCluster) {
        List<QuotaCluster> quotaClusterList = new ArrayList<>();
        if (quotaCluster != null) {
            quotaClusterList.add(quotaCluster);
        }
        return quotaClusterList;
    }

    private static List<QuotaStorage> getQuotaStorage(QuotaStorage quotaStorage) {
        List<QuotaStorage> quotaStorageList = new ArrayList<>();
        if (quotaStorage != null) {
            quotaStorageList.add(quotaStorage);
        }
        return quotaStorageList;
    }

    private static void setQuotaGlobalLimitations(Quota quota) {
        QuotaStorage quotaStorage = new QuotaStorage();
        QuotaCluster quotaCluster = new QuotaCluster();

        // Set Quota storage capacity definition.
        quotaStorage.setStorageSizeGB(10000L);
        quotaStorage.setStorageSizeGBUsage(0d);

        // Set Quota cluster virtual memory definition.
        quotaCluster.setMemSizeMB(16000000L);
        quotaCluster.setMemSizeMBUsage(0L);

        // Set Quota cluster virtual CPU definition.
        quotaCluster.setVirtualCpu(2880);
        quotaCluster.setVirtualCpuUsage(0);

        quota.setGlobalQuotaStorage(quotaStorage);
        quota.setGlobalQuotaCluster(quotaCluster);
    }

    private static Quota createGeneralQuota() {
        Quota quota = new Quota();
        Guid quotaId = Guid.newGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("General quota");
        quota.setThresholdClusterPercentage(80);
        quota.setThresholdStoragePercentage(80);
        quota.setGraceClusterPercentage(20);
        quota.setGraceStoragePercentage(20);
        return quota;
    }
}
