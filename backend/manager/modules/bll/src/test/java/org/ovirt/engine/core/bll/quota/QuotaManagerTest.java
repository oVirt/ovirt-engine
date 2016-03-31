package org.ovirt.engine.core.bll.quota;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.QuotaDao;

@RunWith(MockitoJUnitRunner.class)
public class QuotaManagerTest {

    private static final Guid STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000011");
    private static final Guid STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000012");
    private static final Guid STORAGE_QUOTA_GLOBAL_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000013");
    private static final Guid STORAGE_QUOTA_GLOBAL_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000014");
    private static final Guid STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000015");
    private static final Guid STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000016");
    private static final Guid STORAGE_QUOTA_SPECIFIC_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000017");
    private static final Guid STORAGE_QUOTA_SPECIFIC_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000018");

    private static final Guid VCPU_QUOTA_GLOBAL_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000021");
    private static final Guid VCPU_QUOTA_GLOBAL_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000022");
    private static final Guid VCPU_QUOTA_GLOBAL_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000023");
    private static final Guid VCPU_QUOTA_GLOBAL_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000024");
    private static final Guid VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000025");
    private static final Guid VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000026");
    private static final Guid VCPU_QUOTA_SPECIFIC_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000027");
    private static final Guid VCPU_QUOTA_SPECIFIC_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000028");

    private static final Guid MEM_QUOTA_GLOBAL_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000031");
    private static final Guid MEM_QUOTA_GLOBAL_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000032");
    private static final Guid MEM_QUOTA_GLOBAL_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000033");
    private static final Guid MEM_QUOTA_GLOBAL_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000034");
    private static final Guid MEM_QUOTA_SPECIFIC_NOT_EXCEEDED = new Guid("00000000-0000-0000-0000-000000000035");
    private static final Guid MEM_QUOTA_SPECIFIC_OVER_THRESHOLD = new Guid("00000000-0000-0000-0000-000000000036");
    private static final Guid MEM_QUOTA_SPECIFIC_IN_GRACE = new Guid("00000000-0000-0000-0000-000000000037");
    private static final Guid DESTINATION_GUID = new Guid("00000000-0000-0000-0000-000000002222");

    private static final Guid MEM_QUOTA_SPECIFIC_OVER_GRACE = new Guid("00000000-0000-0000-0000-000000000038");
    private static final long UNLIMITED_STORAGE = QuotaStorage.UNLIMITED;
    private static final int UNLIMITED_VCPU = QuotaCluster.UNLIMITED_VCPU;
    private static final long UNLIMITED_MEM = QuotaCluster.UNLIMITED_MEM;

    private static final String EXPECTED_EMPTY_CAN_DO_MESSAGE = "Can-Do-Action message was expected to be empty";
    private static final String EXPECTED_CAN_DO_MESSAGE = "Can-Do-Action message was expected (result: empty)";
    private static final String EXPECTED_NO_AUDIT_LOG_MESSAGE = "No AuditLog massage was expected";
    private static final String EXPECTED_AUDIT_LOG_MESSAGE = "AuditLog massage was expected";

    @Mock
    private QuotaDao quotaDao;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MinimumPercentageToUpdateQuotaCache, 60)
    );

    @Spy
    private QuotaManager quotaManager = new QuotaManager();
    @Spy
    private QuotaManagerAuditLogger quotaManagerAuditLogger = quotaManager.getQuotaManagerAuditLogger();

    private StoragePool storage_pool = new StoragePool();
    private ArrayList<String> validationMessages = new ArrayList<>();
    private QuotaConsumptionParametersWrapper parametersWrapper;
    private boolean hardEnforcement = true;
    private boolean auditLogWritten = false;
    private int dbCalls = 0;
    private static final String EXPECTED_NUMBER_OF_DB_CALLS = "%d DB calls were expected. %d invoked";

    @Before
    public void testSetup() {
        setStoragePool();
        mockQuotaDao();
        doReturn(quotaDao).when(quotaManager).getQuotaDao();
        doReturn(quotaManagerAuditLogger).when(quotaManager).getQuotaManagerAuditLogger();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0] != null) {
                    auditLogWritten = true;
                }
                return null;
            }
        }).when(quotaManagerAuditLogger).auditLog(any(AuditLogType.class), any(AuditLogableBase.class));

        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.setStoragePool(storage_pool);
        parametersWrapper = new QuotaConsumptionParametersWrapper(auditLogable, validationMessages);
        parametersWrapper.setParameters(new ArrayList<>());
    }

    private void mockQuotaDao() {
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockStorageQuotaGlobalNotExceeded();
            }
        }).when(quotaDao).getById(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED);
        when(quotaDao.getById(STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockStorageQuotaGlobalOverThreshold());
        when(quotaDao.getById(STORAGE_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockStorageQuotaGlobalInGrace());
        when(quotaDao.getById(STORAGE_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockStorageQuotaGlobalOverGrace());
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockStorageQuotaSpecificNotExceeded();
            }
        }).when(quotaDao).getById(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED);
        when(quotaDao.getById(STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockStorageQuotaSpecificOverThreshold());
        when(quotaDao.getById(STORAGE_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockStorageQuotaSpecificInGrace());
        when(quotaDao.getById(STORAGE_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockStorageQuotaSpecificOverGrace());

        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockVCPUQuotaGlobalNotExceeded();
            }
        }).when(quotaDao).getById(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED);
        when(quotaDao.getById(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockVCPUQuotaGlobalOverThreshold());
        when(quotaDao.getById(VCPU_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockVCPUQuotaGlobalInGrace());
        when(quotaDao.getById(VCPU_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockVCPUQuotaGlobalOverGrace());
        when(quotaDao.getById(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED)).thenReturn(mockVCPUQuotaSpecificNotExceeded());
        when(quotaDao.getById(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockVCPUQuotaSpecificOverThreshold());
        when(quotaDao.getById(VCPU_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockVCPUQuotaSpecificInGrace());
        when(quotaDao.getById(VCPU_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockVCPUQuotaSpecificOverGrace());

        when(quotaDao.getById(MEM_QUOTA_GLOBAL_NOT_EXCEEDED)).thenReturn(mockMemQuotaGlobalNotExceeded());
        when(quotaDao.getById(MEM_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockMemQuotaGlobalOverThreshold());
        when(quotaDao.getById(MEM_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockMemQuotaGlobalInGrace());
        when(quotaDao.getById(MEM_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockMemQuotaGlobalOverGrace());
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockMemQuotaSpecificNotExceeded();
            }
        }).when(quotaDao).getById(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED);
        when(quotaDao.getById(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockMemQuotaSpecificOverThreshold());
        when(quotaDao.getById(MEM_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockMemQuotaSpecificInGrace());
        when(quotaDao.getById(MEM_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockMemQuotaSpecificOverGrace());
    }

    private void setStoragePool() {
        storage_pool.setQuotaEnforcementType(hardEnforcement ?
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT :
                QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);
        storage_pool.setId(new Guid("00000000-0000-0000-0000-000000001111"));
    }

    private void assertNotEmptyValidateMessage() {
        assertTrue(EXPECTED_CAN_DO_MESSAGE, !validationMessages.isEmpty());
        validationMessages.clear();
    }

    private void assertEmptyValidateMessage() {
        assertTrue(EXPECTED_EMPTY_CAN_DO_MESSAGE, validationMessages.isEmpty());
    }

    private void assertAuditLogWritten() {
        assertTrue(EXPECTED_AUDIT_LOG_MESSAGE, auditLogWritten);
        auditLogWritten = false;
    }

    private void assertAuditLogNotWritten() {
        assertFalse(EXPECTED_NO_AUDIT_LOG_MESSAGE, auditLogWritten);
    }

    private void assertDbWasCalled(int expectedNumOfCalls) {
        assertTrue(String.format(EXPECTED_NUMBER_OF_DB_CALLS, expectedNumOfCalls, dbCalls), expectedNumOfCalls == dbCalls);
        dbCalls = 0;
    }

    private boolean consumeForStorageQuota(Guid quotaId) {
        QuotaConsumptionParametersWrapper parameters = null;
        try {
            parameters = parametersWrapper.clone();
            parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                    quotaId, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 1d));
        } catch (CloneNotSupportedException e) {  }
        return quotaManager.consume(parameters);
    }

    private boolean consumeForVdsQuota(Guid quotaId) {
        QuotaConsumptionParametersWrapper parameters = null;
        try {
            parameters = parametersWrapper.clone();
            parameters.getParameters().add(new QuotaClusterConsumptionParameter(
                    quotaId, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 1, 1));
        } catch (CloneNotSupportedException e) {}
        return quotaManager.consume(parameters);
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalNotExceeded() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalOverThreshold() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalInGrace() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalOverGrace() throws Exception {
        assertFalse(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificNotExceeded() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificOverThreshold() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificInGrace() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificOverGrace() throws Exception {
        assertFalse(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testDecreaseStorageQuota() throws Exception {
        QuotaConsumptionParametersWrapper parameters = parametersWrapper.clone();

        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_GLOBAL_IN_GRACE,
                null,
                QuotaConsumptionParameter.QuotaAction.RELEASE,
                DESTINATION_GUID,
                8d));
        // decrease the quota usage from 104 GB to 96 (our of 100 GB quota)
        quotaManager.consume(parameters);

        parameters.getParameters().clear();
        // try to consume 1 GB from the same quota (will reach 97 GB out of 100 GB)
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_GLOBAL_IN_GRACE,
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1d));
        assertTrue(quotaManager.consume(parameters));
        assertEmptyValidateMessage();
        validationMessages.clear();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(MEM_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testRemoveFromCache() throws Exception {
        QuotaConsumptionParametersWrapper parameters = parametersWrapper.clone();
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_GLOBAL_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 12d));
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_SPECIFIC_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 12d));
        parameters.getParameters().add(new QuotaClusterConsumptionParameter(
                VCPU_QUOTA_GLOBAL_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 6, 1));
        parameters.getParameters().add(new QuotaClusterConsumptionParameter(
                MEM_QUOTA_SPECIFIC_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 1, 300));

        // ask for a valid consumption (116 out of 120 and 113 out of 120)
        quotaManager.consume(parameters);

        // roll back the consumption
        quotaManager.removeStoragePoolFromCache(storage_pool.getId());

        // reset db
        when(quotaDao.getById(STORAGE_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockStorageQuotaGlobalInGrace());
        when(quotaDao.getById(STORAGE_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockStorageQuotaSpecificInGrace());
        when(quotaDao.getById(VCPU_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockVCPUQuotaGlobalInGrace());
        when(quotaDao.getById(MEM_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockMemQuotaSpecificInGrace());

        // ask again for same consumption
        quotaManager.consume(parameters);
    }

    @Test
    public void testRemoveQuotaFromCache() throws Exception {
        QuotaConsumptionParametersWrapper parameters = parametersWrapper.clone();

        parameters.getParameters().add(new QuotaStorageConsumptionParameter(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED, null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1d));
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED, null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1d));
        parameters.getParameters().add(new QuotaClusterConsumptionParameter(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED, null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1, 1));
        parameters.getParameters().add(new QuotaClusterConsumptionParameter(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED, null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1, 1));

        // add 6 quotas to the cache
        quotaManager.consume(parameters);

        // reset db call flag
        dbCalls = 0;

        // remove all 6 quotas from cache
        quotaManager.removeQuotaFromCache(storage_pool.getId(), STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED);
        quotaManager.removeQuotaFromCache(storage_pool.getId(), STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quotaManager.removeQuotaFromCache(storage_pool.getId(), VCPU_QUOTA_GLOBAL_NOT_EXCEEDED);
        quotaManager.removeQuotaFromCache(storage_pool.getId(), MEM_QUOTA_SPECIFIC_NOT_EXCEEDED);

        // call same quotas again and make sure db was called for every one of them
        quotaManager.consume(parameters);
        assertDbWasCalled(4);
    }

    /**
     * Mock a basic quota. Only the basic data (Id, name, threshold, grace...) is set.
     *
     * @return - basic quota with no limitations
     */
    private Quota mockBasicQuota() {
        dbCalls++;

        // basic data
        Quota quota = new Quota();
        quota.setId(Guid.newGuid());
        quota.setStoragePoolId(storage_pool.getId());
        quota.setDescription("My Quota description");
        quota.setQuotaName("My Quota Name");
        quota.setGraceStoragePercentage(20);
        quota.setGraceClusterPercentage(20);
        quota.setThresholdStoragePercentage(80);
        quota.setThresholdClusterPercentage(80);

        // Enforcement type would be taken from the storage_pool and not from this field.
        // But in case the storage_pool in null this enforcement will be considered.
        quota.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);

        return quota;
    }

    private QuotaStorage getQuotaStorage(long storageSize, double storageSizeUsed) {
        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(storageSize);
        storageQuota.setStorageSizeGBUsage(storageSizeUsed);
        storageQuota.setStorageId(DESTINATION_GUID);
        return storageQuota;
    }

    private List<QuotaStorage> getQuotaStorages(long storageSize, double storageSizeUsed) {
        ArrayList<QuotaStorage> quotaStorages = new ArrayList<>();
        quotaStorages.add(getQuotaStorage(UNLIMITED_STORAGE, 0));
        quotaStorages.add(getQuotaStorage(50, 5));
        quotaStorages.get(0).setStorageId(Guid.newGuid());
        quotaStorages.get(1).setStorageId(Guid.newGuid());
        quotaStorages.add(getQuotaStorage(storageSize, storageSizeUsed));
        return quotaStorages;
    }

    private QuotaCluster getQuotaCluster(int vCpu, int vCpuUsed, long mem, long memUsed) {
        QuotaCluster clusterQuota = new QuotaCluster();
        clusterQuota.setVirtualCpu(vCpu);
        clusterQuota.setVirtualCpuUsage(vCpuUsed);
        clusterQuota.setMemSizeMB(mem);
        clusterQuota.setMemSizeMBUsage(memUsed);
        clusterQuota.setClusterId(DESTINATION_GUID);
        return clusterQuota;
    }

    private List<QuotaCluster> getQuotaClusters(int vCpu, int vCpuUsed, long mem, long memUsed) {
        ArrayList<QuotaCluster> quotaClusters = new ArrayList<>();
        quotaClusters.add(getQuotaCluster(UNLIMITED_VCPU, 0, UNLIMITED_MEM, 0));
        quotaClusters.add(getQuotaCluster(10, 2, 1000, 100));
        quotaClusters.get(0).setClusterId(Guid.newGuid());
        quotaClusters.get(1).setClusterId(Guid.newGuid());
        quotaClusters.add(getQuotaCluster(vCpu, vCpuUsed, mem, memUsed));
        return quotaClusters;
    }

    // ///////////////////// Storage global ////////////////////////////

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED}
     */
    private Quota mockStorageQuotaGlobalNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED);
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 9));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD}
     */
    private Quota mockStorageQuotaGlobalOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD);
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 83));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_GLOBAL_IN_GRACE}
     */
    private Quota mockStorageQuotaGlobalInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_GLOBAL_IN_GRACE);
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 104));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_GLOBAL_OVER_GRACE}
     */
    private Quota mockStorageQuotaGlobalOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_GLOBAL_OVER_GRACE);
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 140));
        return quota;
    }

    // ///////////////////// Storage specific ////////////////////////////

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED}
     */
    private Quota mockStorageQuotaSpecificNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quota.setQuotaStorages(getQuotaStorages(100, 73));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD}
     */
    private Quota mockStorageQuotaSpecificOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD);
        quota.setQuotaStorages(getQuotaStorages(100, 92));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_SPECIFIC_IN_GRACE}
     */
    private Quota mockStorageQuotaSpecificInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_SPECIFIC_IN_GRACE);
        quota.setQuotaStorages(getQuotaStorages(100, 103));
        return quota;
    }

    /**
     * Call by Guid: {@literal STORAGE_QUOTA_SPECIFIC_OVER_GRACE}
     */
    private Quota mockStorageQuotaSpecificOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(STORAGE_QUOTA_SPECIFIC_OVER_GRACE);
        quota.setQuotaStorages(getQuotaStorages(100, 126));
        return quota;
    }

    // ///////////////////// VCPU global ////////////////////////////

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_NOT_EXCEEDED}
     */
    private Quota mockVCPUQuotaGlobalNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED);
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 18, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_OVER_THRESHOLD}
     */
    private Quota mockVCPUQuotaGlobalOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD);
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 92, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_IN_GRACE}
     */
    private Quota mockVCPUQuotaGlobalInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_IN_GRACE);
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 113, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_OVER_GRACE}
     */
    private Quota mockVCPUQuotaGlobalOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_OVER_GRACE);
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 132, UNLIMITED_MEM, 0));
        return quota;
    }

    // ///////////////////// VCPU specific ////////////////////////////

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED}
     */
    private Quota mockVCPUQuotaSpecificNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quota.setQuotaClusters(getQuotaClusters(100, 23, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD}
     */
    private Quota mockVCPUQuotaSpecificOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD);
        quota.setQuotaClusters(getQuotaClusters(100, 96, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_IN_GRACE}
     */
    private Quota mockVCPUQuotaSpecificInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_IN_GRACE);
        quota.setQuotaClusters(getQuotaClusters(100, 105, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_OVER_GRACE}
     */
    private Quota mockVCPUQuotaSpecificOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_OVER_GRACE);
        quota.setQuotaClusters(getQuotaClusters(100, 134, UNLIMITED_MEM, 0));
        return quota;
    }

    // ///////////////////// Mem global ////////////////////////////

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_NOT_EXCEEDED}
     */
    private Quota mockMemQuotaGlobalNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_NOT_EXCEEDED);
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 512));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_OVER_THRESHOLD}
     */
    private Quota mockMemQuotaGlobalOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_OVER_THRESHOLD);
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 1900));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_IN_GRACE}
     */
    private Quota mockMemQuotaGlobalInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_IN_GRACE);
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 2300));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_OVER_GRACE}
     */
    private Quota mockMemQuotaGlobalOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_OVER_GRACE);
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 3000));
        return quota;
    }

    // ///////////////////// Mem specific ////////////////////////////

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_NOT_EXCEEDED}
     */
    private Quota mockMemQuotaSpecificNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 512));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_OVER_THRESHOLD}
     */
    private Quota mockMemQuotaSpecificOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD);
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 2000));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_IN_GRACE}
     */
    private Quota mockMemQuotaSpecificInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_IN_GRACE);
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 2100));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_OVER_GRACE}
     */
    private Quota mockMemQuotaSpecificOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_OVER_GRACE);
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 5000));
        return quota;
    }

}
