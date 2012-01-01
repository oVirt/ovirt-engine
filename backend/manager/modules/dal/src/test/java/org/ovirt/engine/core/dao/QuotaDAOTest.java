package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

public class QuotaDAOTest extends BaseDAOTestCase {
    private QuotaDAO dao;
    private final Long unlimited = new Long("-1");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getQuotaDAO();
    }

    public QuotaDAOTest() throws Exception {
        setUp();
    }

    @Test
    public void testGeneralQuotaLimitations() throws Exception {
        setUp();

        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.NewGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("General quota");
        quota.setThresholdVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdVdsGroup));
        quota.setThresholdStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceVdsGroup));
        quota.setGraceStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceStorage));
        setQuotaGlobalLimitations(quota);
        quota.setQuotaVdsGroups(getQuotaVdsGroup(null));
        quota.setQuotaStorages(getQuotaStorage(null));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());

        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
        assertEquals(quotaEntity.getStoragePoolName(), "rhel6.NFS");
    }

    @Test
    public void testSpecificQuotaLimitations() throws Exception {
        setUp();

        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.NewGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("Specific quota");
        quota.setThresholdVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdVdsGroup));
        quota.setThresholdStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceVdsGroup));
        quota.setGraceStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceStorage));
        quota.setQuotaVdsGroups(getQuotaVdsGroup(getSpecificQuotaVdsGroup(quotaId)));
        quota.setQuotaStorages(getQuotaStorage(getSpecificQuotaStorage(quotaId)));
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
    }

    @Test
    public void testSpecificAndGeneralQuotaLimitations() throws Exception {
        setUp();

        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.NewGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(FixturesTool.STORAGE_POOL_NFS);
        quota.setQuotaName("Watson");
        quota.setDescription("General and specific quota");
        quota.setThresholdVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdVdsGroup));
        quota.setThresholdStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceVdsGroup));
        quota.setGraceStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceStorage));
        quota.setQuotaVdsGroups(getQuotaVdsGroup(getSpecificQuotaVdsGroup(quotaId)));
        quota.setQuotaStorages(getQuotaStorage(null));
        quota.setStorageSizeGB(10000l);
        quota.setStorageSizeGBUsage(0d);
        dao.save(quota);

        Quota quotaEntity = dao.getById(quota.getId());
        assertNotNull(quotaEntity);
        assertEquals(quotaEntity, quota);
    }

    /**
     * Test scenario when there is a specific limitation on a vds group, and we check if there is enough resources on it.<BR/>
     * The returned value from the query, should be the specific limitation and the global usage on the storage pool.
     *
     * @throws Exception
     */
    @Test
    public void testFetchVdsGroupWithUnlimitedGlobalLimitation() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList =
                dao.getQuotaVdsGroupByVdsGroupGuid(FixturesTool.VDS_GROUP_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);

        // Should be two rows of specific vds group
        assertEquals(true, quotaVdsGroupList.size() == 1);

        // Get first specific vds group.
        QuotaVdsGroup quotaVdsGroup = quotaVdsGroupList.get(0);
        assertNotNull(quotaVdsGroup);
        assertEquals(quotaVdsGroup.getMemSizeMB(), unlimited);
        assertEquals(true, quotaVdsGroup.getVirtualCpu().equals(10));
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the global limitation and the global usage on the storage pool.
     *
     * @throws Exception
     */
    @Test
    public void testFetchGlobalQuotaUsageForSpecificVdsGroup() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList =
                dao.getQuotaVdsGroupByVdsGroupGuid(FixturesTool.VDS_GROUP_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);
        QuotaVdsGroup quotaVdsGroup = quotaVdsGroupList.get(0);
        assertNotNull(quotaVdsGroup);
        assertEquals(true, quotaVdsGroupList.size() == 1);
        assertEquals(true, quotaVdsGroup.getMemSizeMBUsage() > 0);
        assertEquals(true, quotaVdsGroup.getVirtualCpuUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(new Integer(100), quotaVdsGroup.getVirtualCpu());
    }

    /**
     * Test scenario when there is a global limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the global limitation and the global usage on the storage pool.
     *
     * @throws Exception
     */
    @Test
    public void testFetchGlobalQuotaUsageForGlobalVdsGroup() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList = dao.getQuotaVdsGroupByVdsGroupGuid(null, FixturesTool.QUOTA_GENERAL);
        QuotaVdsGroup quotaVdsGroup = quotaVdsGroupList.get(0);
        assertEquals(true, quotaVdsGroupList.size() == 1);
        assertNotNull(quotaVdsGroup);
        assertEquals(true, quotaVdsGroup.getMemSizeMBUsage() > 0);

        // Check if the global variable returns when null is initialization.
        assertEquals(new Integer(100), quotaVdsGroup.getVirtualCpu());
    }

    /**
     * Comparing the two previous test results.<BR/>
     * Since there is only global limitation for specific quota that is being checked, the results for fetching per vds
     * group or fetching per storage pool should be the same.
     *
     * @throws Exception
     */
    @Test
    public void testCompareFetchGlobalQuotaForSpecificAndForGlobalVdsGroup() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupGlobalList =
                dao.getQuotaVdsGroupByVdsGroupGuid(null, FixturesTool.QUOTA_GENERAL);
        List<QuotaVdsGroup> quotaVdsGroupSpecificList =
                dao.getQuotaVdsGroupByVdsGroupGuid(FixturesTool.VDS_GROUP_RHEL6_ISCSI, FixturesTool.QUOTA_GENERAL);

        // Check if the global variable returns when null is initialization.
        assertEquals(true, quotaVdsGroupGlobalList.equals(quotaVdsGroupSpecificList));
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check if there is enough resources on a
     * specific vds group.<BR/>
     * The returned value from the query, should be the specific limitation and the specific usage on the vds group.
     *
     * @throws Exception
     */
    @Test
    public void testFetchSpecificQuotaUsageForSpecificVdsGroup() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList =
                dao.getQuotaVdsGroupByVdsGroupGuid(FixturesTool.VDS_GROUP_RHEL6_ISCSI, FixturesTool.QUOTA_SPECIFIC);
        QuotaVdsGroup quotaVdsGroup = quotaVdsGroupList.get(0);
        assertNotNull(quotaVdsGroup);
        assertEquals(true, quotaVdsGroupList.size() == 1);

        // Check if the global variable returns when null is initialization.
        assertEquals(quotaVdsGroup.getVirtualCpu(), new Integer(10));
    }

    /**
     * Test scenario when there is a specific limitation on the storage pool, and we check limitation on the entire
     * storage pool.<BR/>
     * The value that should be returned, is the specific limitations and the specific usage on the storage pool.
     *
     * @throws Exception
     */
    @Test
    public void testFetchSpecificQuotaUsageForGlobalVdsGroup() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList = dao.getQuotaVdsGroupByVdsGroupGuid(null, FixturesTool.QUOTA_SPECIFIC);
        QuotaVdsGroup quotaVdsGroup = quotaVdsGroupList.get(0);
        assertEquals(true, quotaVdsGroupList.size() == 2);
        assertNotNull(quotaVdsGroup);
    }


    @Test
    public void testFetchSpecificAndGeneralQuotaForStorage() throws Exception {
        setUp();

        List<QuotaStorage> quotaStorageList = dao.getQuotaStorageByStorageGuid(null, FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        QuotaStorage quotaStorage = quotaStorageList.get(0);
        assertEquals(true, quotaStorageList.size() == 1);
        assertNotNull(quotaStorage);

        // Check if the global variable returns when null is initialization.
        assertEquals(true, quotaStorage.getStorageSizeGBUsage() > 0);
    }


    @Test
    public void testFetchAllVdsGroupForQuota() throws Exception {
        setUp();

        List<QuotaVdsGroup> quotaVdsGroupList =
                dao.getQuotaVdsGroupByVdsGroupGuid(null, FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(quotaVdsGroupList);
        assertEquals(quotaVdsGroupList.size(), 2);
        for (QuotaVdsGroup quotaVdsGroup : quotaVdsGroupList) {
            if (quotaVdsGroup.getQuotaVdsGroupId()
                    .equals(Guid.createGuidFromString("68c96e11-0aad-4e3a-9091-12897b7f2388"))) {
                assertEquals(quotaVdsGroup.getVirtualCpu(), new Integer("10"));
                assertEquals(quotaVdsGroup.getMemSizeMB(), unlimited);
            }
            else if (quotaVdsGroup.getQuotaVdsGroupId()
                    .equals(Guid.createGuidFromString("68c96e11-0aad-4e3a-9091-12897b7f2389"))) {
                assertEquals(quotaVdsGroup.getVirtualCpu(), new Integer("1000"));
                assertEquals(quotaVdsGroup.getMemSizeMB(), unlimited);
            }
        }
    }

    @Test
    public void testRemoveQuota() throws Exception {
        setUp();
        Quota quota = dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertNotNull(quota);
        dao.remove(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertEquals(null, dao.getById(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL));
        assertEquals(0, dao.getQuotaVdsGroupByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
        assertEquals(0, dao.getQuotaStorageByQuotaGuid(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL).size());
    }

    /**
     * Make Quota specific to be the same as Quota general and specific.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateQuota() throws Exception {
        setUp();
        Quota quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Save quotaName and vdsGroup list for future check.
        String quotaName = "New Temporary name";
        List<QuotaVdsGroup> quotaVdsGroupList =
                getQuotaVdsGroup(getSpecificQuotaVdsGroup(quotaGeneralToSpecific.getId()));
        Long newStorageLimit = new Long("2345");

        // Check before the update, that the fields are not equal.
        assertEquals(quotaName.equals(quotaGeneralToSpecific.getQuotaName()), false);
        assertEquals(quotaVdsGroupList.size() == quotaGeneralToSpecific.getQuotaVdsGroups().size(), false);
        assertEquals(quotaGeneralToSpecific.getStorageSizeGB().equals(newStorageLimit), false);

        // Update
        quotaGeneralToSpecific.setQuotaName(quotaName);
        quotaGeneralToSpecific.setStorageSizeGB(newStorageLimit);
        quotaGeneralToSpecific.setQuotaVdsGroups(quotaVdsGroupList);

        dao.update(quotaGeneralToSpecific);
        quotaGeneralToSpecific = dao.getById(FixturesTool.QUOTA_GENERAL);

        // Check after the update, that the fields are equal now.
        assertEquals(quotaName.equals(quotaGeneralToSpecific.getQuotaName()), true);
        assertEquals(quotaVdsGroupList.size() == quotaGeneralToSpecific.getQuotaVdsGroups().size(), true);
        assertEquals(quotaGeneralToSpecific.getStorageSizeGB().equals(newStorageLimit), true);
    }

    /**
     * Test get Quota by Name, with name of specific Quota.
     *
     * @throws Exception
     */
    @Test
    public void testGetQuotaByExistingName() throws Exception {
        setUp();
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General", FixturesTool.STORAGE_POOL_NFS);
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL)
                .getQuotaName()
                .equals(quotaGeneralToSpecific.getQuotaName()),
                true);
        assertEquals(dao.getById(FixturesTool.QUOTA_GENERAL).getId().equals(quotaGeneralToSpecific.getId()), true);
    }

    /**
     * Test get Quota by Name, with name that does not exist for the storage pool.
     *
     * @throws Exception
     */
    @Test
    public void testGetQuotaByExistingNameWIthNoMatchingStoragePool() throws Exception {
        setUp();
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Quota General", FixturesTool.STORAGE_POOL_RHEL6_ISCSI);
        assertEquals(null, quotaGeneralToSpecific);
    }

    /**
     * Test get Quota by Name, with not existing name.
     *
     * @throws Exception
     */
    @Test
    public void testGetQuotaWithNoExistingName() throws Exception {
        setUp();
        Quota quotaGeneralToSpecific = dao.getQuotaByQuotaName("Any name", FixturesTool.STORAGE_POOL_NFS);
        assertEquals(null, quotaGeneralToSpecific);
    }

    private QuotaStorage getSpecificQuotaStorage(Guid quotaId) {
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setQuotaId(quotaId);
        quotaStorage.setQuotaStorageId(Guid.NewGuid());
        quotaStorage.setStorageId(FixturesTool.STORAGE_DOAMIN_NFS_MASTER);
        quotaStorage.setStorageSizeGB(10000l);
        quotaStorage.setStorageSizeGBUsage(0d);
        return quotaStorage;

    }

    private QuotaVdsGroup getSpecificQuotaVdsGroup(Guid quotaId) {
        QuotaVdsGroup quotaVdsGroup = new QuotaVdsGroup();
        quotaVdsGroup.setQuotaVdsGroupId(Guid.NewGuid());
        quotaVdsGroup.setQuotaId(quotaId);
        quotaVdsGroup.setVdsGroupId(FixturesTool.VDS_GROUP_RHEL6_NFS);
        quotaVdsGroup.setVirtualCpu(2880);
        quotaVdsGroup.setMemSizeMB(16000000l);
        quotaVdsGroup.setVirtualCpuUsage(0);
        quotaVdsGroup.setMemSizeMBUsage(0l);
        return quotaVdsGroup;
    }

    private List<QuotaVdsGroup> getQuotaVdsGroup(QuotaVdsGroup quotaVdsGroup) {
        List<QuotaVdsGroup> quotaVdsGroupList = new ArrayList<QuotaVdsGroup>();
        if (quotaVdsGroup != null) {
            quotaVdsGroupList.add(quotaVdsGroup);
        }
        return quotaVdsGroupList;
    }

    private List<QuotaStorage> getQuotaStorage(QuotaStorage quotaStorage) {
        List<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
        if (quotaStorage != null) {
            quotaStorageList.add(quotaStorage);
        }
        return quotaStorageList;
    }

    private void setQuotaGlobalLimitations(Quota quota) {
        // Set Quota storage capacity definition.
        quota.setStorageSizeGB(10000l);
        quota.setStorageSizeGBUsage(0d);

        // Set Quota cluster virtual memory definition.
        quota.setMemSizeMB(16000000l);
        quota.setMemSizeMBUsage(0l);

        // Set Quota cluster virtual CPU definition.
        quota.setVirtualCpu(2880);
        quota.setVirtualCpuUsage(0);
    }
}
