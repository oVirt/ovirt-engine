package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

public class QuotaDaoTest extends BaseDaoTestCase {
    private QuotaDao dao;
    private static final Long unlimited = -1L;
    private static final int STORAGE_NUM_QUOTAS = 4;
    private static final int VDS_GRUOP_NUM_QUOTAS = 3;
    private EngineSession unprivilegedUserSession;
    private EngineSession privilegedUserSession;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getQuotaDao();

        EngineSessionDao engineDao = dbFacade.getEngineSessionDao();
        unprivilegedUserSession = engineDao.getBySessionId(UNPRIVILEGED_USER_ENGINE_SESSION_ID);
        privilegedUserSession = engineDao.getBySessionId(PRIVILEGED_USER_ENGINE_SESSION_ID);
    }

    @Test
    public void testGeneralQuotaLimitations() throws Exception {
        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.newGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("General quota");
        quota.setThresholdClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaThresholdCluster));
        quota.setThresholdStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaGraceCluster));
        quota.setGraceStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaGraceStorage));
        setQuotaGlobalLimitations(quota);
        quota.setQuotaClusters(getQuotaCluster(null));
        quota.setQuotaStorages(getQuotaStorage(null));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());

        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
        assertEquals(quotaEntity.getStoragePoolName(), "rhel6.NFS");
        assertEquals(quotaEntity.getQuotaEnforcementType(), QuotaEnforcementTypeEnum.DISABLED);
    }

    @Test
    public void testSpecificQuotaLimitations() throws Exception {
        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.newGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("Specific quota");
        quota.setThresholdClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaThresholdCluster));
        quota.setThresholdStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaGraceCluster));
        quota.setGraceStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaGraceStorage));
        quota.setQuotaClusters(getQuotaCluster(getSpecificQuotaCluster(quotaId)));
        quota.setQuotaStorages(getQuotaStorage(getSpecificQuotaStorage(quotaId)));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
    }

    @Test
    public void testSpecificAndGeneralQuotaLimitations() throws Exception {
        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.newGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("General and specific quota");
        quota.setThresholdClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaThresholdCluster));
        quota.setThresholdStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaGraceCluster));
        quota.setGraceStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaGraceStorage));
        quota.setQuotaClusters(getQuotaCluster(getSpecificQuotaCluster(quotaId)));
        quota.setQuotaStorages(getQuotaStorage(null));
        quota.setGlobalQuotaStorage(new QuotaStorage(null, null, null, 10000L, 0d));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
    }

    /**
     * Test scenario when there is a specific limitation on a vds group, and we check if there is enough resources on it.<BR/>
     * The returned value from the query, should be the specific limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchClusterWithUnlimitedGlobalLimitation() throws Exception {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);

        // Should be two rows of specific vds group
        assertEquals(true, quotaClusterList.size() == 1);

        // Get first specific vds group.
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(quotaCluster.getMemSizeMB(), unlimited);
        assertEquals(true, quotaCluster.getVirtualCpu().equals(10));
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the global limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchGlobalQuotaUsageForSpecificCluster() throws Exception {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(true, quotaClusterList.size() == 1);
        assertEquals(true, quotaCluster.getMemSizeMBUsage() > 0);
        assertEquals(true, quotaCluster.getVirtualCpuUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(Integer.valueOf(100), quotaCluster.getVirtualCpu());
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the global limitation and the global usage on the storage pool.
     */
    @Test
    public void testFetchGlobalQuotaUsageForGlobalCluster() throws Exception {
        List<QuotaCluster> quotaClusterList = dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_GENERAL);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertEquals(true, quotaClusterList.size() == 1);
        assertNotNull(quotaCluster);
        assertEquals(true, quotaCluster.getMemSizeMBUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(Integer.valueOf(100), quotaCluster.getVirtualCpu());
    }

    /**
     * Comparing the two previous test results.<BR/>
     * Since there is only global limitation for specific quota that is being checked, the results for fetching per vds
     * group or fetching per storage pool should be the same.
     */
    @Test
    public void testCompareFetchGlobalQuotaForSpecificAndForGlobalCluster() throws Exception {
        List<QuotaCluster> quotaClusterGlobalList =
                dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_GENERAL);
        List<QuotaCluster> quotaClusterSpecificList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);

        // Check if the global variable returns when null is initialization.
        assertEquals(true, quotaClusterGlobalList.equals(quotaClusterSpecificList));
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the specific limitation and the specific usage on the vds group.
     */
    @Test
    public void testFetchSpecificQuotaUsageForSpecificCluster() throws Exception {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertNotNull(quotaCluster);
        assertEquals(true, quotaClusterList.size() == 1);

        // Check if the global variable returns when null is initialization.
        assertEquals(quotaCluster.getVirtualCpu(), Integer.valueOf(10));
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the specific limitations and the specific usage on the storage pool.
     */
    @Test
    public void testFetchSpecificQuotaUsageForGlobalCluster() throws Exception {
        List<QuotaCluster> quotaClusterList = dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_SPECIFIC);
        QuotaCluster quotaCluster = quotaClusterList.get(0);
        assertEquals(true, quotaClusterList.size() == 2);
        assertNotNull(quotaCluster);
    }

    @Test
    public void testFetchSpecificAndGeneralQuotaForStorage() throws Exception {
        List<QuotaStorage> quotaStorageList =
                dao.getQuotaStorageByStorageGuid(null, FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        QuotaStorage quotaStorage = quotaStorageList.get(0);
        assertEquals(true, quotaStorageList.size() == 1);
        assertNotNull(quotaStorage);

        // Check if the global variable returns when null is initialization.
        assertEquals(true, quotaStorage.getStorageSizeGBUsage() > 0);
    }

    @Test
    public void testFetchAllClusterForQuota() throws Exception {
        List<QuotaCluster> quotaClusterList =
                dao.getQuotaClusterByClusterGuid(null, FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(quotaClusterList);
        assertEquals(quotaClusterList.size(), 2);
        for (QuotaCluster quotaCluster : quotaClusterList) {
            if (quotaCluster.getQuotaClusterId()
                    .equals(new Guid("68c96e11-0aad-4e3a-9091-12897b7f2388"))) {
                assertEquals(quotaCluster.getVirtualCpu(), Integer.valueOf(10));
                assertEquals(quotaCluster.getMemSizeMB(), unlimited);
            }
            else if (quotaCluster.getQuotaClusterId()
                    .equals(new Guid("68c96e11-0aad-4e3a-9091-12897b7f2389"))) {
                assertEquals(quotaCluster.getVirtualCpu(), Integer.valueOf(1000));
                assertEquals(quotaCluster.getMemSizeMB(), unlimited);
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
        assertEquals("wrong number of quotas returned", 2, quotaClusterList.size());
        for (QuotaCluster group : quotaClusterList) {
            assertNotNull("VDS ID should not be null in specific mode", group.getClusterId());
            assertNotNull("VDS name should not be null in specific mode", group.getClusterName());
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
        assertEquals("wrong number of quotas returned", 1, quotaClusterList.size());
        for (QuotaCluster group : quotaClusterList) {
            assertEquals("VDS ID should be empty in general mode", Guid.Empty, group.getClusterId());
            assertNull("VDS name should be null in general mode", group.getClusterName());
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
        assertEquals("wrong number of quotas returned", 0, quotaClusterList.size());
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
        assertEquals("wrong number of quotas returned", 1, quotaStorageList.size());
        for (QuotaStorage group : quotaStorageList) {
            assertNotNull("Storage ID should not be null in specific mode", group.getStorageId());
            assertNotNull("Storage name should not be null in specific mode", group.getStorageName());
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
        assertEquals("wrong number of quotas returned", 0, quotaStorageList.size());
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
        assertEquals("wrong number of quotas returned", 1, quotaStorageList.size());
        for (QuotaStorage group : quotaStorageList) {
            assertEquals("Storage ID should not be null in general mode", Guid.Empty, group.getStorageId());
            assertNull("Storage name should not be null in general mode", group.getStorageName());
        }
    }

    @Test
    public void testRemoveQuota() throws Exception {
        Quota quota = dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertNotNull(quota);
        dao.remove(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertEquals(null, dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL));
        assertEquals(0, dao.getQuotaClusterByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
        assertEquals(0, dao.getQuotaStorageByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
    }

    /**
     * Make Quota specific to be the same as Quota general and specific.
     */
    @Test
    public void testUpdateQuota() throws Exception {
        Quota quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Save quotaName and cluster list for future check.
        String quotaName = "New Temporary name";
        List<QuotaCluster> quotaClusterList =
                getQuotaCluster(getSpecificQuotaCluster(quotaGeneralToSpecific.getId()));
        Long newStorageLimit = 2345L;

        // Check before the update, that the fields are not equal.
        assertEquals(quotaName.equals(quotaGeneralToSpecific.getQuotaName()), false);
        assertEquals(quotaClusterList.size() == quotaGeneralToSpecific.getQuotaClusters().size(), false);
        assertEquals(quotaGeneralToSpecific.getGlobalQuotaStorage().getStorageSizeGB().equals(newStorageLimit), false);

        // Update
        quotaGeneralToSpecific.setQuotaName(quotaName);
        quotaGeneralToSpecific.getGlobalQuotaStorage().setStorageSizeGB(newStorageLimit);
        quotaGeneralToSpecific.setQuotaClusters(quotaClusterList);

        dao.update(quotaGeneralToSpecific);
        quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Check after the update, that the fields are equal now.
        assertEquals(quotaName.equals(quotaGeneralToSpecific.getQuotaName()), true);
        assertEquals(quotaClusterList.size() == quotaGeneralToSpecific.getQuotaClusters().size(), true);
        assertEquals(quotaGeneralToSpecific.getGlobalQuotaStorage().getStorageSizeGB().equals(newStorageLimit), true);
    }

    /**
     * Test get Quota by Name, with name of specific Quota.
     */
    @Test
    public void testGetQuotaByExistingName() throws Exception {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General");
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL)
                .getQuotaName()
                .equals(quotaGeneralToSpecific.getQuotaName()),
                true);
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL).getId().equals(quotaGeneralToSpecific.getId()), true);
    }

    /**
     * Test get Quota by Name, with name of specific Quota.
     */
    @Test
    public void testGetQuotaByAdElementId() throws Exception {
        List<Quota> quotaByAdElementIdList =
                dao.getQuotaByAdElementId(FixturesTool.USER_EXISTING_ID, FixturesTool.STORAGE_POOL_NFS, false);

        // Check if quota general has been fetched.
        assertEquals(quotaByAdElementIdList.get(0).getQuotaName(), "Quota General");
    }

    /**
     * Test get all Quotas in the setup
     */
    @Test
    public void testGetFetchAllQuotaInTheSetup() throws Exception {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(null);
        assertEquals(5, quotaList.size());
    }

    /**
     * Test get Quota by storage pool Id
     */
    @Test
    public void testGetFetchForSpecificStoragePool() throws Exception {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(FixturesTool.STORAGE_POOL_NFS);
        assertEquals(5, quotaList.size());
    }

    /**
     * Test get Quota by storage pool Id, for storage pool with no quotas in it.
     */
    @Test
    public void testFetchStoragePoolWithNoQuota() throws Exception {
        List<Quota> quotaList = dao.getQuotaByStoragePoolGuid(Guid.newGuid());
        assertEquals(true, quotaList.size() == 0);
    }

    /**
     * Test get Quota by Name, with name that does not exist for the storage pool.
     */
    @Test
    public void testGetQuotaByExistingNameWIthNoMatchingStoragePool() throws Exception {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General2");
        assertEquals(null, quotaGeneralToSpecific);
    }

    /**
     * Test get Quota by Name, with not existing name.
     */
    @Test
    public void testGetQuotaWithNoExistingName() throws Exception {
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Any name");
        assertEquals(null, quotaGeneralToSpecific);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with an existing storage domain
     */
    @Test
    public void testGetRelevantQuotasExistingStorage() throws Exception {
        // there is one specific quota and all the general ones defined on this storage domain
        assertGetAllRelevantQuoatsForStorage(FixturesTool.STORAGE_DOAMIN_NFS_MASTER, STORAGE_NUM_QUOTAS);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with a storage domain with no specific quotas
     */
    @Test
    public void testGetRelevantQuotasExistingStorageNoSpecificQuotas() throws Exception {
        // there are no specific quotas, but all the general quotas relate to the storage pool containing this domain
        assertGetAllRelevantQuoatsForStorage(FixturesTool.STORAGE_DOAMIN_NFS_ISO, STORAGE_NUM_QUOTAS - 1);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with a non existing storage domain
     */
    @Test
    public void testGetRelevantQuotasNonExistingStorage() throws Exception {
        // There is no such storgae, so no quotas are defined on it
        assertGetAllRelevantQuoatsForStorage(Guid.newGuid(), 0);
    }

    /**
     * Asserts that {@link #expectedQuotas} are relevant for the given {@link #storageId}
     */
    private void assertGetAllRelevantQuoatsForStorage(Guid storageId, int expectedQuotas) {
        assertNotNull(privilegedUserSession);
        List<Quota> quotas = dao.getAllRelevantQuotasForStorage(storageId, privilegedUserSession.getId(), false);
        assertEquals("Wrong number of quotas retuend", expectedQuotas, quotas.size());
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with an existing VDS Group
     */
    @Test
    public void testGetRelevantQuotasExistingCluster() throws Exception {
        // there is one specific quota and all the general ones defined on this VDS Group
        assertGetAllRelevantQuoatsForCluster(FixturesTool.CLUSTER_RHEL6_NFS, VDS_GRUOP_NUM_QUOTAS);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with a VDS Group domain with no specific quotas
     */
    @Test
    public void testGetRelevantQuotasExistingClusterNoSpecificQuotas() throws Exception {
        // there are no specific quotas, but all the general quotas relate to the storage pool containing this group
        assertGetAllRelevantQuoatsForCluster(FixturesTool.CLUSTER_RHEL6_NFS_NO_SPECIFIC_QUOTAS, VDS_GRUOP_NUM_QUOTAS - 1);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} with a non existing VDS Group
     */
    @Test
    public void testGetRelevantQuotasNonExistingCluster() throws Exception {
        // There is no such storgae, so no quotas are defined on it
        assertGetAllRelevantQuoatsForCluster(Guid.newGuid(), 0);
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForStorage(Guid)} fetching quota for user
     * without privileges for quota.
     */
    @Test
    public void testGetRelevantStorageQuotaForUserWithoutPrivileges() throws Exception {
        assertNotNull(unprivilegedUserSession);
        List<Quota> quotas = dao.getAllRelevantQuotasForStorage(FixturesTool.STORAGE_DOAMIN_NFS_MASTER, unprivilegedUserSession.getId(), true);
        assertEquals("Unprivileged user is not allowed to fetch for quota", 0, quotas.size());
    }

    /**
     * Test {@link QuotaDao#getAllRelevantQuotasForCluster(Guid)} fetching quota for user
     * without privileges for quota.
     */
    @Test
    public void testGetRelevantClusterQuotaForUserWithoutPrivileges() throws Exception {
        assertNotNull(unprivilegedUserSession);
        List<Quota> quotas = dao.getAllRelevantQuotasForCluster(FixturesTool.CLUSTER_RHEL6_NFS,
                unprivilegedUserSession.getId(),
                true);
        assertEquals("Unprivileged user is not allowed to fetch for quota", 0, quotas.size());
    }

    /**
     * Asserts that {@link #expectedQuotas} are relevant for the given {@link #clusterId}
     */
    private void assertGetAllRelevantQuoatsForCluster(Guid clusterId, int expectedQuotas) {
        assertNotNull(privilegedUserSession);
        List<Quota> quotas = dao.getAllRelevantQuotasForCluster(clusterId, privilegedUserSession.getId(), false);
        assertEquals("Wrong number of quotas retuend", expectedQuotas, quotas.size());
    }

    private static QuotaStorage getSpecificQuotaStorage(Guid quotaId) {
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setQuotaId(quotaId);
        quotaStorage.setQuotaStorageId(Guid.newGuid());
        quotaStorage.setStorageId(FixturesTool.STORAGE_DOAMIN_NFS_MASTER);
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
}
