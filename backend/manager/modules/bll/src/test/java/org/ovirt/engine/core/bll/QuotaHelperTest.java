package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

@RunWith(MockitoJUnitRunner.class)
public class QuotaHelperTest {
    @Mock
    private VdsGroupDAO vdsGroupDAO;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private QuotaDAO quotaDAO;

    private QuotaHelper quotaHelper;

    private final Guid storagePoolUUID = Guid.NewGuid();
    private final String storagePoolName = "Storage pool name";

    private final Guid firstStorageDomainUUID = Guid.NewGuid();
    private final String firstStorageDomainName = "First storage domain name";
    private final Guid secondStorageDomainUUID = Guid.NewGuid();
    private final String secondStorageDomainName = "Second storage domain name";
    private final Guid firstVdsGroupUUID = Guid.NewGuid();
    private final Guid secondVdsGroupUUID = Guid.NewGuid();
    private final String quotaName = "New Quota Name";
    private storage_pool storagePool = new storage_pool();
    private storage_domains firstStorageDomains = new storage_domains();
    private storage_domains secondStorageDomains = new storage_domains();

    @Before
    public void testSetup() {
        quotaHelper = getQuotaHelper();
        mockDbFacade();
        mockConfig();
    }

    private void mockConfig() {
        doReturn(80).when(quotaHelper).getQuotaThresholdVdsGroup();
        doReturn(80).when(quotaHelper).getQuotaThresholdStorage();
        doReturn(20).when(quotaHelper).getQuotaGraceVdsGroup();
        doReturn(20).when(quotaHelper).getQuotaGraceStorage();
    }

    private void mockDbFacade() {
        doReturn(vdsGroupDAO).when(quotaHelper).getVdsGroupDao();
        doReturn(storageDomainDAO).when(quotaHelper).getStorageDomainDao();
        doReturn(storagePoolDAO).when(quotaHelper).getStoragePoolDao();
        doReturn(quotaDAO).when(quotaHelper).getQuotaDAO();

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
    public void testGetUnlimitedQuota() throws Exception {
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), false);
        assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getGlobalQuotaStorage().getStorageSizeGB());
        assertEquals(QuotaHelper.UNLIMITED.intValue(), quotaUnlimited.getGlobalQuotaVdsGroup()
                .getVirtualCpu()
                .intValue());
        assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getGlobalQuotaVdsGroup().getMemSizeMB());
    }

    @Test
    public void testGetDefaultQuotaName() throws Exception {
        List<Quota> listQuota = new ArrayList<Quota>();
        listQuota.add(mockGeneralStorageQuota());
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(listQuota);
        String quotaDefaultName = quotaHelper.getDefaultQuotaName(mockStoragePool());
        String desiredQuotaDefaultName = "Quota_Def_Storage pool name";
        assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenDefaultNameAlreadyExist() throws Exception {
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
        assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExist() throws Exception {
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
        assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExistNotOrdered() throws Exception {
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
        assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaNameWhenManyDefaultQuotasExistMixedNumbered() throws Exception {
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
        assertEquals(desiredQuotaDefaultName, quotaDefaultName);
    }

    @Test
    public void testGetDefaultQuotaForDCWithDefaultQuota() throws Exception {
        when(quotaDAO.getDefaultQuotaByStoragePoolId(storagePoolUUID)).thenReturn(mockGeneralStorageQuota());
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true);
        assertQuotaUnlimited(quotaUnlimited);
        assertQuotaUnlimitedName(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaForDCWithDefaultQuotaWithNoReuse() throws Exception {
        when(quotaDAO.getDefaultQuotaByStoragePoolId(storagePoolUUID)).thenReturn(mockGeneralStorageQuota());
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true, false).getFirst();
        assertQuotaUnlimited(quotaUnlimited);
        assertQuotaUnlimitedName(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaForDCWithDefaultQuotaWithReuse() throws Exception {
        when(quotaDAO.getDefaultQuotaByStoragePoolId(storagePoolUUID)).thenReturn(mockUnlimitedQuota());
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true, true).getFirst();
        assertQuotaUnlimited(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaToDCWithoutDefaultQuota() throws Exception {
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true);
        assertQuotaUnlimited(quotaUnlimited);
        assertQuotaUnlimitedName(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaToDCWithoutDefaultQuotaWithNoReuse() throws Exception {
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true, false).getFirst();
        assertQuotaUnlimited(quotaUnlimited);
        assertQuotaUnlimitedName(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaToDCWithoutDefaultQuotaWithReuse() throws Exception {
        when(quotaDAO.getDefaultQuotaByStoragePoolId(storagePoolUUID)).thenReturn(null);
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool(), true, true).getFirst();
        assertQuotaUnlimited(quotaUnlimited);
    }

    @Test
    public void testGetDefaultQuotaWithNullID() throws Exception {
        storage_pool mockStoragePool = mockStoragePool();
        mockStoragePool.setId(null);
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool, true);
        assertNull(quotaUnlimited);
        verifyZeroInteractions(quotaDAO);
    }

    @Test
    public void testGetDefaultQuotaWithNullIDWithNoResue() throws Exception {
        storage_pool mockStoragePool = mockStoragePool();
        mockStoragePool.setId(null);
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool, true, false).getFirst();
        assertNull(quotaUnlimited);
        verifyZeroInteractions(quotaDAO);
    }

    @Test
    public void testGetDefaultQuotaWithNullIDWithResue() throws Exception {
        storage_pool mockStoragePool = mockStoragePool();
        mockStoragePool.setId(null);
        Quota quotaUnlimited = quotaHelper.getUnlimitedQuota(mockStoragePool, true, true).getFirst();
        assertNull(quotaUnlimited);
        verifyZeroInteractions(quotaDAO);
    }

    protected void assertQuotaUnlimited(Quota quotaUnlimited) {
        assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getGlobalQuotaStorage().getStorageSizeGB());
        assertEquals(QuotaHelper.UNLIMITED.intValue(), quotaUnlimited.getGlobalQuotaVdsGroup()
                .getVirtualCpu()
                .intValue());
        assertEquals(QuotaHelper.UNLIMITED, quotaUnlimited.getGlobalQuotaVdsGroup().getMemSizeMB());
        assertEquals(true, quotaUnlimited.getIsDefaultQuota());
    }

    protected void assertQuotaUnlimitedName(Quota quotaUnlimited) {
        assertEquals("Quota_Def_" + storagePoolName, quotaUnlimited.getQuotaName());
    }

    @Test
    public void testStorageEmptyQuota() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        quota.setGlobalQuotaStorage(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testGlobalAndSpecificCpuForVdsGroup() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();

        // Set new global quota for vds group.
        QuotaVdsGroup quotaVdsGroup = new QuotaVdsGroup();
        quotaVdsGroup.setVirtualCpu(2);
        quota.setGlobalQuotaVdsGroup(quotaVdsGroup);

        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString()));
        assertFalse(isQuotaValid);
    }

    @Test
    public void testGlobalAndSpecificRamForVdsGroup() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();

        // Set new global quota for vds group.
        QuotaVdsGroup quotaVdsGroup = new QuotaVdsGroup();
        quotaVdsGroup.setMemSizeMB(2l);
        quota.setGlobalQuotaVdsGroup(quotaVdsGroup);

        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString()));
        assertFalse(isQuotaValid);
    }

    @Test
    public void testSpecificValidQuotaForVdsGroup() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testCpuEmptyQuota() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setVirtualCpu(null);
        }
        quota.setGlobalQuotaVdsGroup(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testRamEmptyQuota() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockSpecificVdsGroupQuota();
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setMemSizeMB(null);
        }
        quota.setGlobalQuotaVdsGroup(null);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testSpecificRamWithGlobalCpuQuota() throws Exception {
        List<String> messages = new ArrayList<String>();

        // Get specific quota for vds Quota, which will be configured as general.
        Quota quota = mockSpecificVdsGroupQuota();

        // Iterate over all the vds groups and set the virtual cpu to null.
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            quotaVdsGroup.setVirtualCpu(null);
        }

        // Set the global limitation of cpu to specific number.
        // Set new global quota for vds group.
        QuotaVdsGroup quotaVdsGroup = new QuotaVdsGroup();
        quotaVdsGroup.setVirtualCpu(20);
        quota.setGlobalQuotaVdsGroup(quotaVdsGroup);

        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertFalse(isQuotaValid);
    }

    @Test
    public void testMultiSpecificVdsGroupQuota() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockMultiSpecificVdsGroupQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testAddSpecificVdsGroupAndStorageQuota() throws Exception {
        List<String> messages = new ArrayList<String>();

        // Get quota for specific Vds Groups
        Quota quota = mockMultiSpecificVdsGroupQuota();

        // Get specific storage quota to initialize the vds group quota fetched before with its storage list.
        Quota quotaStorage = mockMultiSpecificStorageQuota();
        quota.setQuotaStorages(quotaStorage.getQuotaStorages());

        // Set global limitation with null value to specify the quota is only for specific storage limitations.
        quota.setGlobalQuotaStorage(null);

        // Mock storage domains with enough space for request.
        mockStorageDomains();
        assertEquals(true, quotaHelper.checkQuotaValidationForAddEdit(quota, messages));
    }

    @Test
    public void testValidGlobalStorageLimitation() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        mockStorageDomains();
        assertEquals(true, quotaHelper.checkQuotaValidationForAddEdit(quota, messages));
    }

    @Test
    public void testValidSpecificStorageLimitation() throws Exception {
        List<String> messages = new ArrayList<String>();
        firstStorageDomains.setused_disk_size(0);
        firstStorageDomains.setavailable_disk_size(100);
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(false);
        when(storageDomainDAO.getForStoragePool(firstStorageDomainUUID, storagePoolUUID)).thenReturn(firstStorageDomains);
        boolean isQuotaValid = quotaHelper.checkQuotaValidationForAddEdit(quota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testUpdateSameQuotaWithoutChangingName() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota mockQuota = mockGeneralStorageQuota();
        when(quotaDAO.getQuotaByQuotaName(quotaName)).thenReturn(mockQuota);
        boolean isQuotaValid = quotaHelper.checkQuotaNameExisting(mockQuota, messages);
        assertTrue(isQuotaValid);
    }

    @Test
    public void testQuotaWithSameNameExists() throws Exception {
        List<String> messages = new ArrayList<String>();
        Quota mockQuota = mockGeneralStorageQuota();
        when(quotaDAO.getQuotaByQuotaName(quotaName)).thenReturn(mockQuota);
        Quota sameMockedQuotaWithDifferentId = mockGeneralStorageQuota();
        boolean isQuotaValid = quotaHelper.checkQuotaNameExisting(sameMockedQuotaWithDifferentId, messages);
        assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NAME_ALREADY_EXISTS.toString()));
        assertFalse(isQuotaValid);
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
        quotaSpecific.setGlobalQuotaVdsGroup(null);
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
        quotaSpecific.setGlobalQuotaVdsGroup(null);
        return quotaSpecific;
    }

    private Quota mockMultiSpecificStorageQuota() {
        Quota quotaSpecific = mockGeneralStorageQuota();
        quotaSpecific.setGlobalQuotaStorage(null);
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
        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(100l);
        storageQuota.setStorageSizeGBUsage(0d);
        generalQuota.setGlobalQuotaStorage(storageQuota);

        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(0);
        vdsGroupQuota.setVirtualCpuUsage(0);
        vdsGroupQuota.setMemSizeMB(0l);
        vdsGroupQuota.setMemSizeMBUsage(0l);
        generalQuota.setGlobalQuotaVdsGroup(vdsGroupQuota);

        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        generalQuota.setIsDefaultQuota(true);
        return generalQuota;
    }

    private Quota mockGeneralVdsGroupQuota() {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName("New Quota Name");
        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(0l);
        storageQuota.setStorageSizeGBUsage(0d);
        generalQuota.setGlobalQuotaStorage(storageQuota);

        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(3);
        vdsGroupQuota.setVirtualCpuUsage(0);
        vdsGroupQuota.setMemSizeMB(100l);
        vdsGroupQuota.setMemSizeMBUsage(0l);
        generalQuota.setGlobalQuotaVdsGroup(vdsGroupQuota);

        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        return generalQuota;
    }

    private Quota mockUnlimitedQuota() {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName(quotaName);

        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(QuotaHelper.UNLIMITED);
        storageQuota.setStorageSizeGBUsage(0d);
        generalQuota.setGlobalQuotaStorage(storageQuota);

        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(QuotaHelper.UNLIMITED.intValue());
        vdsGroupQuota.setVirtualCpuUsage(0);
        vdsGroupQuota.setMemSizeMB(QuotaHelper.UNLIMITED);
        vdsGroupQuota.setMemSizeMBUsage(0l);
        generalQuota.setGlobalQuotaVdsGroup(vdsGroupQuota);

        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        generalQuota.setIsDefaultQuota(true);
        return generalQuota;
    }
}
