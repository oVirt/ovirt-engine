package org.ovirt.engine.core.bll.quota;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.QuotaDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class QuotaManagerTest {

    private static final Guid DESTINATION_GUID = new Guid("00000000-0000-0000-0000-000000002222");

    private static final long UNLIMITED_STORAGE = QuotaStorage.UNLIMITED;
    private static final int UNLIMITED_VCPU = QuotaCluster.UNLIMITED_VCPU;
    private static final long UNLIMITED_MEM = QuotaCluster.UNLIMITED_MEM;

    private static final Guid DEFAULT_QUOTA_FOR_STORAGE_POOL = new Guid("00000000-0000-0000-0000-123456789000");

    private static final String EXPECTED_EMPTY_VALIDATE_MESSAGE = "Validate message was expected to be empty";
    private static final String EXPECTED_VALIDATE_MESSAGE = "Validate message was expected (result: empty)";

    @Mock
    private QuotaDao quotaDao;

    @Mock
    private AuditLogDirector auditLogDirector;

    @InjectMocks
    @Spy
    private QuotaManager quotaManager = new QuotaManager();

    private StoragePool storage_pool = new StoragePool();
    private ArrayList<String> validationMessages = new ArrayList<>();
    private CommandBase<?> command;
    private int dbCalls = 0;
    private static final String EXPECTED_NUMBER_OF_DB_CALLS = "%d DB calls were expected. %d invoked";

    private Quota quota;

    @BeforeEach
    public void setUp() {
        storage_pool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);
        storage_pool.setId(new Guid("00000000-0000-0000-0000-000000001111"));

        when(quotaDao.getById(DEFAULT_QUOTA_FOR_STORAGE_POOL)).thenReturn(mockDefaultQuota());
        when(quotaDao.getDefaultQuotaForStoragePool(any())).thenReturn(mockDefaultQuota());

        doReturn(quotaDao).when(quotaManager).getQuotaDao();

        ActionParametersBase param = new ActionParametersBase();
        command = new CommandBase<ActionParametersBase>(
                param, CommandContext.createContext(param.getSessionId())) {
            @Override
            protected void executeCommand() {}

            @Override
            public List<PermissionSubject> getPermissionCheckSubjects() {
                return null;
            }
        };

        command.setStoragePool(storage_pool);
        command.getReturnValue().setValidationMessages(validationMessages);

        quota = mockBasicQuota();
        when(quotaDao.getById(quota.getId())).thenReturn(quota);
    }

    private void assertNotEmptyValidateMessage() {
        assertTrue(!validationMessages.isEmpty(), EXPECTED_VALIDATE_MESSAGE);
        validationMessages.clear();
    }

    private void assertEmptyValidateMessage() {
        assertTrue(validationMessages.isEmpty(), EXPECTED_EMPTY_VALIDATE_MESSAGE);
    }

    private void assertAuditLogWritten(AuditLogType type) {
        verify(auditLogDirector).log(any(), eq(type));
    }

    private void assertAuditLogNotWritten() {
        verify(auditLogDirector, never()).log(any(), any());
    }

    private boolean consumeForStorageQuota(double requestedStorageGB) {
        return quotaManager.consume(command, Collections.singletonList(new QuotaStorageConsumptionParameter(
                quota.getId(), QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, requestedStorageGB
        )));
    }

    private boolean consumeForClusterQuota(int requestedCpu, long requestedMemory) {
        return quotaManager.consume(command, Collections.singletonList(new QuotaClusterConsumptionParameter(
                quota.getId(), QuotaConsumptionParameter.QuotaAction.CONSUME, DESTINATION_GUID, requestedCpu, requestedMemory
        )));
    }

    @Test
    public void testConsumeStorageQuotaGlobalNotExceeded() {
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 9));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeStorageQuotaGlobalOverThreshold() {
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 83));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
    }

    @Test
    public void testConsumeStorageQuotaGlobalInGrace() {
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 104));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
    }

    @Test
    public void testConsumeStorageQuotaGlobalOverGrace() {
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 140));

        assertFalse(consumeForStorageQuota(1d));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT);
    }

    @Test
    public void testConsumeStorageQuotaSpecificNotExceeded() {
        quota.setQuotaStorages(getQuotaStorages(100, 73));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeStorageQuotaSpecificOverThreshold() {
        quota.setQuotaStorages(getQuotaStorages(100, 92));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
    }

    @Test
    public void testConsumeStorageQuotaSpecificInGrace() {
        quota.setQuotaStorages(getQuotaStorages(100, 103));

        assertTrue(consumeForStorageQuota(1d));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
    }

    @Test
    public void testConsumeStorageQuotaSpecificOverGrace() {
        quota.setQuotaStorages(getQuotaStorages(100, 126));

        assertFalse(consumeForStorageQuota(1d));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT);
    }

    @Test
    public void testDecreaseStorageQuota() {
        Quota quota = mockBasicQuota();
        quota.setGlobalQuotaStorage(getQuotaStorage(100, 104));
        when(quotaDao.getById(quota.getId())).thenReturn(quota);

        // decrease the quota usage from 104 GB to 96 (our of 100 GB quota)
        quotaManager.consume(command, Collections.singletonList(new QuotaStorageConsumptionParameter(
                quota.getId(),
                QuotaConsumptionParameter.QuotaAction.RELEASE,
                DESTINATION_GUID,
                8d)));

        // try to consume 1 GB from the same quota (will reach 97 GB out of 100 GB)
        assertTrue(quotaManager.consume(command, Collections.singletonList(createStorageConsumption(quota.getId(), 1d))));
        assertEmptyValidateMessage();
        validationMessages.clear();
    }

    @Test
    public void testConsumeClusterQuotaForVCPUGlobalNotExceeded() {
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 18, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeClusterQuotaForVCPUGlobalOverThreshold() {
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 92, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
    }

    @Test
    public void testConsumeClusterQuotaForVCPUGlobalInGrace() {
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 113, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForVCPUGlobalOverGrace() {
        quota.setGlobalQuotaCluster(getQuotaCluster(100, 132, UNLIMITED_MEM, 0));

        assertFalse(consumeForClusterQuota(1, 1));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForVCPUSpecificNotExceeded() {
        quota.setQuotaClusters(getQuotaClusters(100, 23, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeClusterQuotaForVCPUSpecificOverThreshold() {
        quota.setQuotaClusters(getQuotaClusters(100, 96, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
    }

    @Test
    public void testConsumeClusterQuotaForVCPUSpecificInGrace() {
        quota.setQuotaClusters(getQuotaClusters(100, 105, UNLIMITED_MEM, 0));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForVCPUSpecificOverGrace() {
        quota.setQuotaClusters(getQuotaClusters(100, 134, UNLIMITED_MEM, 0));

        assertFalse(consumeForClusterQuota(1, 1));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForMemGlobalNotExceeded() {
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 512));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeClusterQuotaForMemGlobalOverThreshold() {
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 1900));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
    }

    @Test
    public void testConsumeClusterQuotaForMemGlobalInGrace() {
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 2300));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForMemGlobalOverGrace() {
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, 2048, 3000));

        assertFalse(consumeForClusterQuota(1, 1));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForMemSpecificNotExceeded() {
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 512));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testConsumeClusterQuotaForMemSpecificOverThreshold() {
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 2000));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
    }

    @Test
    public void testConsumeClusterQuotaForMemSpecificInGrace() {
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 2100));

        assertTrue(consumeForClusterQuota(1, 1));
        assertEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
    }

    @Test
    public void testConsumeClusterQuotaForMemSpecificOverGrace() {
        quota.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 5000));

        assertFalse(consumeForClusterQuota(1, 1));
        assertNotEmptyValidateMessage();
        assertAuditLogWritten(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT);
    }

    @Test
    public void testRemoveFromCache() {
        Quota quota1 = mockBasicQuota();
        quota1.setGlobalQuotaStorage(getQuotaStorage(100, 104));
        when(quotaDao.getById(quota1.getId())).thenReturn(quota1);

        Quota quota2 = mockBasicQuota();
        quota2.setQuotaStorages(getQuotaStorages(100, 103));
        when(quotaDao.getById(quota2.getId())).thenReturn(quota2);

        List<QuotaConsumptionParameter> parameters = new ArrayList<>();

        parameters.add(createStorageConsumption(quota1.getId(), 12d));
        parameters.add(createStorageConsumption(quota2.getId(), 12d));

        quotaManager.consume(command, parameters);

        quotaManager.removeStoragePoolFromCache(storage_pool.getId());

        quotaManager.consume(command, parameters);

        verify(quotaDao, times(2)).getById(quota1.getId());
        verify(quotaDao, times(2)).getById(quota2.getId());
    }

    @Test
    public void testRemoveQuotaFromCache() {
        Quota quota1 = mockBasicQuota();
        quota1.setGlobalQuotaStorage(getQuotaStorage(100, 9));
        when(quotaDao.getById(quota1.getId())).thenReturn(quota1);

        Quota quota2 = mockBasicQuota();
        quota2.setQuotaClusters(getQuotaClusters(UNLIMITED_VCPU, 0, 2048, 512));
        when(quotaDao.getById(quota2.getId())).thenReturn(quota2);

        List<QuotaConsumptionParameter> parameters = new ArrayList<>();

        parameters.add(createStorageConsumption(quota1.getId(), 1d));
        parameters.add(createClusterConsumption(quota2.getId(), 1, 1));

        // add 2 quotas to the cache
        quotaManager.consume(command, parameters);

        // remove only 1 quota from cache
        quotaManager.removeQuotaFromCache(storage_pool.getId(), quota1.getId());

        // call same quotas again and make sure db was called for the first one
        quotaManager.consume(command, parameters);

        verify(quotaDao, times(2)).getById(quota1.getId());
        verify(quotaDao, times(1)).getById(quota2.getId());
    }

    @Test
    public void testUseDefaultQuotaStorage() {
        assertTrue(quotaManager.consume(command,
                Collections.singletonList(createStorageConsumption(null, 1d))));

        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    @Test
    public void testUseDefaultQuotaCluster() {
        assertTrue(quotaManager.consume(command,
                Collections.singletonList(createClusterConsumption(null, 1, 1))));

        assertEmptyValidateMessage();
        assertAuditLogNotWritten();
    }

    /**
     * Mock a basic quota. Only the basic data (Id, name, threshold, grace...) is set.
     *
     * @return - basic quota with no limitations
     */
    private Quota mockBasicQuota() {
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

    private QuotaStorageConsumptionParameter createStorageConsumption(Guid quotaId, double requestedStorageGB) {
        return new QuotaStorageConsumptionParameter(quotaId,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                requestedStorageGB);
    }

    private QuotaClusterConsumptionParameter createClusterConsumption(Guid quotaId, int cpu, long memory) {
        return new QuotaClusterConsumptionParameter(quotaId,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                DESTINATION_GUID,
                cpu,
                memory);
    }

    /**
     * Call by Guid: {@literal DEFAULT_QUOTA_FOR_STORAGE_POOL}
     */
    private Quota mockDefaultQuota() {
        Quota quota = mockBasicQuota();
        quota.setId(DEFAULT_QUOTA_FOR_STORAGE_POOL);
        quota.setGlobalQuotaCluster(getQuotaCluster(UNLIMITED_VCPU, 0, UNLIMITED_MEM, 0));
        quota.setGlobalQuotaStorage(getQuotaStorage(UNLIMITED_STORAGE, 0));
        return quota;
    }
}
