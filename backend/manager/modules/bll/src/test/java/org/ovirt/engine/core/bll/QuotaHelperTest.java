package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Config.class })
public class QuotaHelperTest {
    @Mock
    private DbFacade db;

    @Mock
    private VdsGroupDAO vdsGroupDAO;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private VmTemplateDAO vmTemplateDAO;

    @Mock
    private QuotaDAO quotaDAO;

    @Mock
    private StorageDomainDynamicDAO storageDomainDynamicDAO;

    private final Guid storagePoolUUID = Guid.NewGuid();
    private final String storagePoolName = "Storage pool name";

    private final Guid firstStorageDomainUUID = Guid.NewGuid();
    private final String firstStorageDomainName = "First storage domain name";
    private final Guid secondStorageDomainUUID = Guid.NewGuid();
    private final String secondStorageDomainName = "Second storage domain name";
    private final Guid firstVdsGroupUUID = Guid.NewGuid();
    private final Guid secondVdsGroupUUID = Guid.NewGuid();
    private final String quotaName = "New Quota Name";
    storage_pool storagePool = new storage_pool();
    storage_domains firstStorageDomains = new storage_domains();
    storage_domains secondStorageDomains = new storage_domains();

    public QuotaHelperTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Config.class);
    }

    @Before
    public void testSetup() {
        mockDbFacade();
        mockConfig();
    }

    private static void mockConfig() {
        mockStatic(Config.class);
        when(Config.<Integer> GetValue(ConfigValues.QuotaThresholdVdsGroup)).thenReturn(80);
        when(Config.<Integer> GetValue(ConfigValues.QuotaThresholdStorage)).thenReturn(80);
        when(Config.<Integer> GetValue(ConfigValues.QuotaGraceVdsGroup)).thenReturn(20);
        when(Config.<Integer> GetValue(ConfigValues.QuotaGraceStorage)).thenReturn(20);
    }

    private void mockDbFacade() {
        mockStatic(DbFacade.class);
        Mockito.when(DbFacade.getInstance()).thenReturn(db);
        when(db.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
        when(db.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        when(db.getStoragePoolDAO()).thenReturn(storagePoolDAO);
        when(db.getVmTemplateDAO()).thenReturn(vmTemplateDAO);
        when(db.getQuotaDAO()).thenReturn(quotaDAO);
        when(db.getStorageDomainDynamicDAO()).thenReturn(storageDomainDynamicDAO);

        // Mock business entities.
        mockStoragePool();
        mockFirstStorageDomain();
        mockSecondStorageDomain();

        // Mock DAO procedures
        when(storageDomainDAO.getForStoragePool(firstStorageDomainUUID, storagePoolUUID)).thenReturn(firstStorageDomains);
        when(storageDomainDAO.getForStoragePool(secondStorageDomainUUID, storagePoolUUID)).thenReturn(secondStorageDomains);
        when(storageDomainDAO.getAllForStoragePool(storagePoolUUID)).thenReturn(getEmptyStorageDomainList());
        when(storagePoolDAO.get(storagePoolUUID)).thenReturn(mockStoragePool());
    }

    private static List<storage_domains> getEmptyStorageDomainList() {
        return new ArrayList<storage_domains>();
    }

    @Test
    public void testAddUnlimitedQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), false);
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getStorageSizeGB());
        Assert.assertTrue(QuotaHelper.UNLIMITED.intValue() == quotaUnlimited.getVirtualCpu().intValue());
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getMemSizeMB());
    }

    @Test
    public void testGetDefaultQuotaName() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<Quota> listQuota = new ArrayList<Quota>();
        listQuota.add(mockGeneralStorageQuota());
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name";
        Assert.assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenDefaultNameAlreadyExist() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<Quota> listQuota = new ArrayList<Quota>();

        // Add storage quota.
        Quota quota = mockGeneralStorageQuota();
        quota.setQuotaName("Quota_Def_Storage pool name");
        listQuota.add(quota);

        // Add the list to the mock up when fetching list of quota.
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);

        // Get and check the retrieved default quota name.
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name_1";
        Assert.assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExist() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<Quota> listQuota = new ArrayList<Quota>();

        // Add storage quota.
        Quota generalStorageQuota = mockGeneralStorageQuota();
        generalStorageQuota.setQuotaName("Quota_Def_Storage pool name");
        listQuota.add(generalStorageQuota);

        // Add vds group quota.
        Quota generalVdsGroupQuota = mockGeneralVdsGroupQuota();
        generalVdsGroupQuota.setQuotaName("Quota_Def_Storage pool name_1");
        listQuota.add(generalVdsGroupQuota);

        // Add the list to the mock up when fetching list of quota.
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);

        // Get and check the retrieved default quota name.
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name_2";
        Assert.assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExistNotOrdered() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<Quota> listQuota = new ArrayList<Quota>();

        // Add storage quota.
        Quota generalStorageQuota = mockGeneralStorageQuota();
        generalStorageQuota.setQuotaName("Quota_Def_Storage pool name_1");
        listQuota.add(generalStorageQuota);

        // Add vds group quota.
        Quota generalVdsGroupQuota = mockGeneralVdsGroupQuota();
        generalVdsGroupQuota.setQuotaName("Quota_Def_Storage pool name");
        listQuota.add(generalVdsGroupQuota);

        // Add the list to the mock up when fetching list of quota.
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);

        // Get and check the retrieved default quota name.
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name_2";
        Assert.assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExistMixedNumbered() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<Quota> listQuota = new ArrayList<Quota>();

        // Add storage quota.
        Quota generalStorageQuota = mockGeneralStorageQuota();
        generalStorageQuota.setQuotaName("Quota_Def_Storage pool name_53");
        listQuota.add(generalStorageQuota);

        // Add vds group quota.
        Quota generalVdsGroupQuota = mockGeneralVdsGroupQuota();
        generalVdsGroupQuota.setQuotaName("Quota_Def_Storage pool name_2");
        listQuota.add(generalVdsGroupQuota);

        // Add the list to the mock up when fetching list of quota.
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);

        // Get and check the retrieved default quota name.
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name_54";
        Assert.assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testAddDefaultQuotaToDCWithDefaultQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        when(quotaDAO.getDefaultQuotaByStoragePoolId(storagePoolUUID)).thenReturn(mockGeneralStorageQuota());
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true);
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getStorageSizeGB());
        Assert.assertTrue(QuotaHelper.UNLIMITED.intValue() == quotaUnlimited.getVirtualCpu().intValue());
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getMemSizeMB());
        Assert.assertEquals("Quota_Def_" + storagePoolName, quotaUnlimited.getQuotaName());
        Assert.assertEquals(true, quotaUnlimited.getIsDefaultQuota());
    }

    @Test
    public void testAddDefaultQuotaToDCWithoutDefaultQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true);
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getStorageSizeGB());
        Assert.assertTrue(QuotaHelper.UNLIMITED.intValue() == quotaUnlimited.getVirtualCpu().intValue());
        Assert.assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getMemSizeMB());
        Assert.assertEquals("Quota_Def_" + storagePoolName, quotaUnlimited.getQuotaName());
        Assert.assertEquals(true, quotaUnlimited.getIsDefaultQuota());
    }

    @Test
    public void testAddDefaultQuotaWithNullID() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        storage_pool mockStoragePool = mockStoragePool();
        mockStoragePool.setId(null);
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool, true);
        Assert.assertEquals(null, quotaUnlimited);
    }

    @Test
    public void testStorageEmptyQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        quota.setStorageSizeGB(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testGlobalAndSpecificCpuForVdsGroup() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        quota.setVirtualCpu(2);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString()));
        Assert.assertFalse(isQuotaValid);
    }

    @Test
    public void testGlobalAndSpecificRamForVdsGroup() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        quota.setMemSizeMB(2l);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString()));
        Assert.assertFalse(isQuotaValid);
    }

    @Test
    public void testSpecificValidQuotaForVdsGroup() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testCpuEmptyQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setVirtualCpu(null);
        }
        quota.setVirtualCpu(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testRamEmptyQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setMemSizeMB(null);
        }
        quota.setMemSizeMB(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testSpecificRamWithGlobalCpuQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();

        // Get specific quota for vds Quota, which will be configured as general.
        Quota quota = mockSpecificVdsGroupQuota();

        // Iterate over all the vds groups and set the virtual cpu to null.
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setVirtualCpu(null);
            // TODO : Should I also update usage.
        }

        // Set the global limitation of cpu to specific number.
        quota.setVirtualCpu(20);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testMultiSpecificVdsGroupQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockMultiSpecificVdsGroupQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testAddSpecificVdsGroupAndStorageQuota() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();

        // Get quota for specific Vds Groups
        Quota quota = mockMultiSpecificVdsGroupQuota();

        // Get specific storage quota to initialize the vds group quota fetched before with its storage list.
        Quota quotaStorage = mockMultiSpecificStorageQuota();
        quota.setQuotaStorages(quotaStorage.getQuotaStorages());

        // Set global limitation with null value to specify the quota is only for specific storage limitations.
        quota.setStorageSizeGB(null);

        // Mock storage domains with enough space for request.
        mockStorageDomains();
        Assert.assertEquals(true, quotaHelper.checkQuotaValidationForAddEdit(quota, messages));
    }

    @Test
    public void testValidGlobalStorageLimitation() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        mockStorageDomains();
        Assert.assertEquals(true, quotaHelper.checkQuotaValidationForAddEdit(quota, messages));
    }

    @Test
    public void testValidSpecificStorageLimitation() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        firstStorageDomains.setused_disk_size(0);
        firstStorageDomains.setavailable_disk_size(100);
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        when(storageDomainDAO.getForStoragePool(firstStorageDomainUUID, storagePoolUUID)).thenReturn(firstStorageDomains);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testUpdateSameQuotaWithoutChangingName() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota mockQuota = mockGeneralStorageQuota();
        when(quotaDAO.getQuotaByQuotaName(quotaName)).thenReturn(mockQuota);
        boolean isQuotaValid = quotaHelper.checkQuotaNameExisting(mockQuota, messages);
        Assert.assertTrue(isQuotaValid);
    }

    @Test
    public void testQuotaWithSameNameExists() throws Exception {
        QuotaHelper quotaHelper = getQuotaHelper();
        List<String> messages = new ArrayList<String>();
        Quota mockQuota = mockGeneralStorageQuota();
        when(quotaDAO.getQuotaByQuotaName(quotaName)).thenReturn(mockQuota);
        Quota sameMockedQuotaWithDifferentId = mockGeneralStorageQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaNameExisting(sameMockedQuotaWithDifferentId, messages);
        Assert.assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NAME_ALREADY_EXISTS.toString()));
        Assert.assertFalse(isQuotaValid);
    }

    private static QuotaHelper getQuotaHelper() {
        QuotaHelper quotaHelper = QuotaHelper.getInstance();
        return spy(quotaHelper);
    }

    private storage_pool mockStoragePool() {
        storagePool = new storage_pool();
        storagePool.setname(storagePoolName);
        storagePool.setId(storagePoolUUID);
        return storagePool;
    }

    private void mockFirstStorageDomain() {
        firstStorageDomains = new storage_domains();
        firstStorageDomains.setstorage_name(firstStorageDomainName);
        firstStorageDomains.setId(firstStorageDomainUUID);
    }

    private void mockSecondStorageDomain() {
        secondStorageDomains = new storage_domains();
        secondStorageDomains.setstorage_name(secondStorageDomainName);
        secondStorageDomains.setId(secondStorageDomainUUID);
    }

    private Quota mockMultiSpecificVdsGroupQuota() {
        Quota quotaSpecific = mockGeneralVdsGroupQuota();

        // Set the global parameters to be null in the specific Quota.
        quotaSpecific.setVirtualCpu(null);
        quotaSpecific.setVirtualCpuUsage(null);
        quotaSpecific.setMemSizeMB(null);
        quotaSpecific.setMemSizeMBUsage(null);

        List<QuotaVdsGroup> quotaVdsGroupList = new ArrayList<QuotaVdsGroup>();

        // Set first quota vds group.
        QuotaVdsGroup firstQuotaVdsGroup = new QuotaVdsGroup();
        firstQuotaVdsGroup.setVirtualCpu(50);
        firstQuotaVdsGroup.setVirtualCpu(0);
        firstQuotaVdsGroup.setMemSizeMB(50l);
        firstQuotaVdsGroup.setMemSizeMBUsage(0l);
        firstQuotaVdsGroup.setVdsGroupId(firstVdsGroupUUID);

        // Set second quota vds group.
        QuotaVdsGroup secondQuotaVdsGroup = new QuotaVdsGroup();
        secondQuotaVdsGroup.setVirtualCpu(30);
        secondQuotaVdsGroup.setVirtualCpu(0);
        secondQuotaVdsGroup.setMemSizeMB(30l);
        secondQuotaVdsGroup.setMemSizeMBUsage(0l);
        secondQuotaVdsGroup.setVdsGroupId(secondVdsGroupUUID);

        // Add two vds groups to the quota list.
        quotaVdsGroupList.add(firstQuotaVdsGroup);
        quotaVdsGroupList.add(secondQuotaVdsGroup);

        quotaSpecific.setQuotaVdsGroups(quotaVdsGroupList);
        return quotaSpecific;
    }

    private void mockStorageDomains() {
        // Set list of storages domain to have sum of available disk size enough for the quota limitations.
        List<storage_domains> storageDomains = new ArrayList<storage_domains>();
        firstStorageDomains.setused_disk_size(100);
        firstStorageDomains.setavailable_disk_size(50);
        secondStorageDomains.setused_disk_size(100);
        secondStorageDomains.setavailable_disk_size(50);
        storageDomains.add(firstStorageDomains);
        storageDomains.add(secondStorageDomains);

        when(storageDomainDAO.getAllForStoragePool(storagePoolUUID)).thenReturn(storageDomains);
    }

    private Quota mockSpecificVdsGroupQuota() {
        Quota quotaSpecific = mockGeneralVdsGroupQuota();
        List<QuotaVdsGroup> quotaVdsGroupList = new ArrayList<QuotaVdsGroup>();

        // Set first quota vds group.
        QuotaVdsGroup firstQuotaVdsGroup = new QuotaVdsGroup();
        firstQuotaVdsGroup.setVirtualCpu(50);
        firstQuotaVdsGroup.setVirtualCpu(0);
        firstQuotaVdsGroup.setMemSizeMB(50l);
        firstQuotaVdsGroup.setMemSizeMBUsage(0l);
        firstQuotaVdsGroup.setVdsGroupId(firstVdsGroupUUID);

        // Add vds group to the quota list.
        quotaVdsGroupList.add(firstQuotaVdsGroup);

        quotaSpecific.setQuotaVdsGroups(quotaVdsGroupList);

        // Set the global parameters to be null in the specific Quota.
        quotaSpecific.setVirtualCpu(null);
        quotaSpecific.setVirtualCpuUsage(null);
        quotaSpecific.setMemSizeMB(null);
        quotaSpecific.setMemSizeMBUsage(null);

        return quotaSpecific;
    }

    private Quota mockMultiSpecificStorageQuota() {
        Quota quotaSpecific = mockGeneralStorageQuota();
        quotaSpecific.setStorageSizeGB(null);
        List<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();

        // Set first quota Storage.
        QuotaStorage firstQuotaStorage = new QuotaStorage();
        firstQuotaStorage.setStorageSizeGB(50l);
        firstQuotaStorage.setStorageSizeGBUsage(0d);
        firstQuotaStorage.setStorageId(firstStorageDomainUUID);

        // Set second quota Storage.
        QuotaStorage secondQuotaStorage = new QuotaStorage();
        secondQuotaStorage.setStorageSizeGB(50l);
        secondQuotaStorage.setStorageSizeGBUsage(0d);
        secondQuotaStorage.setStorageId(secondStorageDomainUUID);

        quotaStorageList.add(firstQuotaStorage);
        quotaStorageList.add(secondQuotaStorage);

        quotaSpecific.setQuotaStorages(quotaStorageList);
        return quotaSpecific;
    }

    private Quota mockGeneralStorageQuota() {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName(quotaName);
        generalQuota.setStorageSizeGB(100l);
        generalQuota.setStorageSizeGBUsage(0d);
        generalQuota.setVirtualCpu(0);
        generalQuota.setVirtualCpuUsage(0);
        generalQuota.setMemSizeMB(0l);
        generalQuota.setMemSizeMBUsage(0l);
        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        generalQuota.setIsDefaultQuota(true);
        return generalQuota;
    }

    private Quota mockGeneralVdsGroupQuota() {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName("New Quota Name");
        generalQuota.setStorageSizeGB(0l);
        generalQuota.setStorageSizeGBUsage(0d);
        generalQuota.setVirtualCpu(3);
        generalQuota.setVirtualCpuUsage(0);
        generalQuota.setMemSizeMB(100l);
        generalQuota.setMemSizeMBUsage(0l);
        generalQuota.setStorageSizeGB(0l);
        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        return generalQuota;
    }
}
