package org.ovirt.engine.core.bll.quota;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

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
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

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
    private static final int UNLIMITED_VCPU = QuotaVdsGroup.UNLIMITED_VCPU;
    private static final long UNLIMITED_MEM = QuotaVdsGroup.UNLIMITED_MEM;

    private static final String EXPECTED_EMPTY_CAN_DO_MESSAGE = "Can-Do-Action message was expected to be empty";
    private static final String EXPECTED_CAN_DO_MESSAGE = "Can-Do-Action message was expected (result: empty)";
    private static final String EXPECTED_NO_AUDIT_LOG_MESSAGE = "No AuditLog massage was expected";
    private static final String EXPECTED_AUDIT_LOG_MESSAGE = "AuditLog massage was expected";

    @Mock
    private QuotaDAO quotaDAO;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MinimumPercentageToUpdateQuotaCache, 60)
    );

    @Spy
    private QuotaManager quotaManager = new QuotaManager();
    @Spy
    private QuotaManagerAuditLogger quotaManagerAuditLogger = quotaManager.getQuotaManagerAuditLogger();

    private StoragePool storage_pool = new StoragePool();
    private ArrayList<String> canDoActionMessages = new ArrayList<String>();
    private QuotaConsumptionParametersWrapper parametersWrapper;
    private boolean hardEnforcement = true;
    private boolean auditLogWritten = false;
    private int dbCalls = 0;
    private static final String EXPECTED_NUMBER_OF_DB_CALLS = "%d DB calls were expected. %d invoked";

    @Before
    public void testSetup() {
        setStoragePool();
        mockQuotaDAO();
        doReturn(quotaDAO).when(quotaManager).getQuotaDAO();
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
        parametersWrapper = new QuotaConsumptionParametersWrapper(auditLogable, canDoActionMessages);
        parametersWrapper.setParameters(new ArrayList<QuotaConsumptionParameter>());
    }

    private void mockQuotaDAO() {
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockStorageQuotaGlobalNotExceeded();
            }
        }).when(quotaDAO).getById(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED);
        when(quotaDAO.getById(STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockStorageQuotaGlobalOverThreshold());
        when(quotaDAO.getById(STORAGE_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockStorageQuotaGlobalInGrace());
        when(quotaDAO.getById(STORAGE_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockStorageQuotaGlobalOverGrace());
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockStorageQuotaSpecificNotExceeded();
            }
        }).when(quotaDAO).getById(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED);
        when(quotaDAO.getById(STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockStorageQuotaSpecificOverThreshold());
        when(quotaDAO.getById(STORAGE_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockStorageQuotaSpecificInGrace());
        when(quotaDAO.getById(STORAGE_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockStorageQuotaSpecificOverGrace());

        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockVCPUQuotaGlobalNotExceeded();
            }
        }).when(quotaDAO).getById(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED);
        when(quotaDAO.getById(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockVCPUQuotaGlobalOverThreshold());
        when(quotaDAO.getById(VCPU_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockVCPUQuotaGlobalInGrace());
        when(quotaDAO.getById(VCPU_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockVCPUQuotaGlobalOverGrace());
        when(quotaDAO.getById(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED)).thenReturn(mockVCPUQuotaSpecificNotExceeded());
        when(quotaDAO.getById(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockVCPUQuotaSpecificOverThreshold());
        when(quotaDAO.getById(VCPU_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockVCPUQuotaSpecificInGrace());
        when(quotaDAO.getById(VCPU_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockVCPUQuotaSpecificOverGrace());

        when(quotaDAO.getById(MEM_QUOTA_GLOBAL_NOT_EXCEEDED)).thenReturn(mockMemQuotaGlobalNotExceeded());
        when(quotaDAO.getById(MEM_QUOTA_GLOBAL_OVER_THRESHOLD)).thenReturn(mockMemQuotaGlobalOverThreshold());
        when(quotaDAO.getById(MEM_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockMemQuotaGlobalInGrace());
        when(quotaDAO.getById(MEM_QUOTA_GLOBAL_OVER_GRACE)).thenReturn(mockMemQuotaGlobalOverGrace());
        doAnswer(new Answer<Quota>() {
            @Override
            public Quota answer(InvocationOnMock invocation) throws Throwable {
                return mockMemQuotaSpecificNotExceeded();
            }
        }).when(quotaDAO).getById(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED);
        when(quotaDAO.getById(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD)).thenReturn(mockMemQuotaSpecificOverThreshold());
        when(quotaDAO.getById(MEM_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockMemQuotaSpecificInGrace());
        when(quotaDAO.getById(MEM_QUOTA_SPECIFIC_OVER_GRACE)).thenReturn(mockMemQuotaSpecificOverGrace());
    }

    private void setStoragePool() {
        storage_pool.setQuotaEnforcementType(hardEnforcement ?
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT :
                QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);
        storage_pool.setId(new Guid("00000000-0000-0000-0000-000000001111"));
    }

    private void assertNotEmptyCanDoActionMessage() {
        assertTrue(EXPECTED_CAN_DO_MESSAGE, !canDoActionMessages.isEmpty());
        canDoActionMessages.clear();
    }

    private void assertEmptyCanDoActionMessage() {
        assertTrue(EXPECTED_EMPTY_CAN_DO_MESSAGE, canDoActionMessages.isEmpty());
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
            parameters.getParameters().add(new QuotaVdsGroupConsumptionParameter(
                    quotaId, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 1, 1));
        } catch (CloneNotSupportedException e) {}
        return quotaManager.consume(parameters);
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalNotExceeded() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalOverThreshold() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalInGrace() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaGlobalOverGrace() throws Exception {
        assertFalse(consumeForStorageQuota(STORAGE_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificNotExceeded() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificOverThreshold() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificInGrace() throws Exception {
        assertTrue(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetStorageQuotaSpecificOverGrace() throws Exception {
        assertFalse(consumeForStorageQuota(STORAGE_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
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
        assertEmptyCanDoActionMessage();
        canDoActionMessages.clear();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUGlobalOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(VCPU_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForVCPUSpecificOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(VCPU_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_GLOBAL_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemGlobalOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(MEM_QUOTA_GLOBAL_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificNotExceeded() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED));
        assertEmptyCanDoActionMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificOverThreshold() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificInGrace() throws Exception {
        assertTrue(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_IN_GRACE));
        assertEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testValidateAndSetClusterQuotaForMemSpecificOverGrace() throws Exception {
        assertFalse(consumeForVdsQuota(MEM_QUOTA_SPECIFIC_OVER_GRACE));
        assertNotEmptyCanDoActionMessage();
        assertAuditLogWritten();
    }

    @Test
    public void testRemoveFromCache() throws Exception {
        QuotaConsumptionParametersWrapper parameters = parametersWrapper.clone();
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_GLOBAL_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 12d));
        parameters.getParameters().add(new QuotaStorageConsumptionParameter(
                STORAGE_QUOTA_SPECIFIC_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 12d));
        parameters.getParameters().add(new QuotaVdsGroupConsumptionParameter(
                VCPU_QUOTA_GLOBAL_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 6, 1));
        parameters.getParameters().add(new QuotaVdsGroupConsumptionParameter(
                MEM_QUOTA_SPECIFIC_IN_GRACE, null, QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, 1, 300));

        // ask for a valid consumption (116 out of 120 and 113 out of 120)
        quotaManager.consume(parameters);

        // roll back the consumption
        quotaManager.removeStoragePoolFromCache(storage_pool.getId());

        // reset db
        when(quotaDAO.getById(STORAGE_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockStorageQuotaGlobalInGrace());
        when(quotaDAO.getById(STORAGE_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockStorageQuotaSpecificInGrace());
        when(quotaDAO.getById(VCPU_QUOTA_GLOBAL_IN_GRACE)).thenReturn(mockVCPUQuotaGlobalInGrace());
        when(quotaDAO.getById(MEM_QUOTA_SPECIFIC_IN_GRACE)).thenReturn(mockMemQuotaSpecificInGrace());

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
        parameters.getParameters().add(new QuotaVdsGroupConsumptionParameter(VCPU_QUOTA_GLOBAL_NOT_EXCEEDED, null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                1, 1));
        parameters.getParameters().add(new QuotaVdsGroupConsumptionParameter(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED, null,
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
        quota.setGraceVdsGroupPercentage(20);
        quota.setThresholdStoragePercentage(80);
        quota.setThresholdVdsGroupPercentage(80);

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
        ArrayList<QuotaStorage> quotaStorages = new ArrayList<QuotaStorage>();
        quotaStorages.add(getQuotaStorage(UNLIMITED_STORAGE, 0));
        quotaStorages.add(getQuotaStorage(50, 5));
        quotaStorages.get(0).setStorageId(Guid.newGuid());
        quotaStorages.get(1).setStorageId(Guid.newGuid());
        quotaStorages.add(getQuotaStorage(storageSize, storageSizeUsed));
        return quotaStorages;
    }

    private QuotaVdsGroup getQuotaVdsGroup(int vCpu, int vCpuUsed, long mem, long memUsed) {
        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(vCpu);
        vdsGroupQuota.setVirtualCpuUsage(vCpuUsed);
        vdsGroupQuota.setMemSizeMB(mem);
        vdsGroupQuota.setMemSizeMBUsage(memUsed);
        vdsGroupQuota.setVdsGroupId(DESTINATION_GUID);
        return vdsGroupQuota;
    }

    private List<QuotaVdsGroup> getQuotaVdsGroups(int vCpu, int vCpuUsed, long mem, long memUsed) {
        ArrayList<QuotaVdsGroup> quotaVdsGroups = new ArrayList<QuotaVdsGroup>();
        quotaVdsGroups.add(getQuotaVdsGroup(UNLIMITED_VCPU, 0, UNLIMITED_MEM, 0));
        quotaVdsGroups.add(getQuotaVdsGroup(10, 2, 1000, 100));
        quotaVdsGroups.get(0).setVdsGroupId(Guid.newGuid());
        quotaVdsGroups.get(1).setVdsGroupId(Guid.newGuid());
        quotaVdsGroups.add(getQuotaVdsGroup(vCpu, vCpuUsed, mem, memUsed));
        return quotaVdsGroups;
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
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(100, 18, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_OVER_THRESHOLD}
     */
    private Quota mockVCPUQuotaGlobalOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_OVER_THRESHOLD);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(100, 92, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_IN_GRACE}
     */
    private Quota mockVCPUQuotaGlobalInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_IN_GRACE);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(100, 113, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_GLOBAL_OVER_GRACE}
     */
    private Quota mockVCPUQuotaGlobalOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_GLOBAL_OVER_GRACE);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(100, 132, UNLIMITED_MEM, 0));
        return quota;
    }

    // ///////////////////// VCPU specific ////////////////////////////

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED}
     */
    private Quota mockVCPUQuotaSpecificNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(100, 23, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD}
     */
    private Quota mockVCPUQuotaSpecificOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_OVER_THRESHOLD);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(100, 96, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_IN_GRACE}
     */
    private Quota mockVCPUQuotaSpecificInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_IN_GRACE);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(100, 105, UNLIMITED_MEM, 0));
        return quota;
    }

    /**
     * Call by Guid: {@literal VCPU_QUOTA_SPECIFIC_OVER_GRACE}
     */
    private Quota mockVCPUQuotaSpecificOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(VCPU_QUOTA_SPECIFIC_OVER_GRACE);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(100, 134, UNLIMITED_MEM, 0));
        return quota;
    }

    // ///////////////////// Mem global ////////////////////////////

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_NOT_EXCEEDED}
     */
    private Quota mockMemQuotaGlobalNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_NOT_EXCEEDED);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(UNLIMITED_VCPU, 0, 2048, 512));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_OVER_THRESHOLD}
     */
    private Quota mockMemQuotaGlobalOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_OVER_THRESHOLD);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(UNLIMITED_VCPU, 0, 2048, 1900));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_IN_GRACE}
     */
    private Quota mockMemQuotaGlobalInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_IN_GRACE);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(UNLIMITED_VCPU, 0, 2048, 2300));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_GLOBAL_OVER_GRACE}
     */
    private Quota mockMemQuotaGlobalOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_GLOBAL_OVER_GRACE);
        quota.setGlobalQuotaVdsGroup(getQuotaVdsGroup(UNLIMITED_VCPU, 0, 2048, 3000));
        return quota;
    }

    // ///////////////////// Mem specific ////////////////////////////

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_NOT_EXCEEDED}
     */
    private Quota mockMemQuotaSpecificNotExceeded() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_NOT_EXCEEDED);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(UNLIMITED_VCPU, 0, 2048, 512));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_OVER_THRESHOLD}
     */
    private Quota mockMemQuotaSpecificOverThreshold() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_OVER_THRESHOLD);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(UNLIMITED_VCPU, 0, 2048, 2000));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_IN_GRACE}
     */
    private Quota mockMemQuotaSpecificInGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_IN_GRACE);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(UNLIMITED_VCPU, 0, 2048, 2100));
        return quota;
    }

    /**
     * Call by Guid: {@literal MEM_QUOTA_SPECIFIC_OVER_GRACE}
     */
    private Quota mockMemQuotaSpecificOverGrace() {
        Quota quota = mockBasicQuota();
        quota.setId(MEM_QUOTA_SPECIFIC_OVER_GRACE);
        quota.setQuotaVdsGroups(getQuotaVdsGroups(UNLIMITED_VCPU, 0, 2048, 5000));
        return quota;
    }

}
