package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;

public class RemoveQuotaCommandTest extends BaseCommandTest {

    private final Guid generalGuidQuota = Guid.newGuid();
    private final Guid defaultQuotaGuid = Guid.newGuid();
    private final Guid storagePoolUUID = Guid.newGuid();

    @Mock
    private QuotaDao quotaDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private QuotaManager quotaManager;

    /**
     * The command under test.
     */
    private RemoveQuotaCommand command;

    @Before
    public void testSetup() {
        mockQuotaDao();
        mockVmDao();
        mockStoragePoolDao();
        mockInjections();
    }

    private void mockVmDao() {
        // Mock VM Dao getAllVmsRelatedToQuotaId.
        List<VM> newList = new ArrayList<>();
        when(vmDao.getAllVmsRelatedToQuotaId(generalGuidQuota)).thenReturn(newList);
    }

    private void mockQuotaDao() {
        when(quotaDao.getById(generalGuidQuota)).thenReturn(mockStorageQuota(generalGuidQuota));

        Quota defaultQuota = mockStorageQuota(defaultQuotaGuid);
        defaultQuota.setDefault(true);

        when(quotaDao.getById(defaultQuotaGuid)).thenReturn(defaultQuota);

        List<Quota> quotaList = new ArrayList<>();
        quotaList.add(new Quota());
        quotaList.add(new Quota());
        when(quotaDao.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(quotaList);
        when(quotaDao.isQuotaInUse(any(Quota.class))).thenReturn(false);
    }

    private void mockStoragePoolDao() {
        when(storagePoolDao.get(any(Guid.class))).thenReturn(mockStoragePool());
    }

    private void mockInjections() {
        doNothing().when(quotaManager).removeQuotaFromCache(any(Guid.class), any(Guid.class));
    }

    @Test
    public void testExecuteCommand() throws Exception {
        RemoveQuotaCommand removeQuotaCommand = createCommand(generalGuidQuota);
        removeQuotaCommand.executeCommand();
        assertTrue(removeQuotaCommand.getSucceeded());
    }

    @Test
    public void testValidateCommand() throws Exception {
        RemoveQuotaCommand removeQuotaCommand = createCommand(generalGuidQuota);
        ValidateTestUtils.runAndAssertValidateSuccess(removeQuotaCommand);
    }

    @Test
    public void testFailToRemoveDefaultQuota() {
        RemoveQuotaCommand removeQuotaCommand = createCommand(defaultQuotaGuid);
        ValidateTestUtils.runAndAssertValidateFailure(removeQuotaCommand,
                EngineMessage.ACTION_TYPE_FAILED_QUOTA_DEFAULT_CANNOT_BE_CHANGED);
    }

    private RemoveQuotaCommand createCommand(Guid guid) {
        IdParameters param = new IdParameters(guid);
        command = spy(new RemoveQuotaCommand(param, null));
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
        doReturn(quotaDao).when(command).getQuotaDao();
        doReturn(vmDao).when(command).getVmDao();
        doReturn(quotaManager).when(command).getQuotaManager();
        return command;
    }

    private StoragePool mockStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolUUID);
        storagePool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);
        return storagePool;
    }

    private Quota mockStorageQuota(Guid guid) {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName("New Quota Name");
        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(100L);
        storageQuota.setStorageSizeGBUsage(0d);
        generalQuota.setGlobalQuotaStorage(storageQuota);

        QuotaCluster clusterQuota = new QuotaCluster();
        clusterQuota.setVirtualCpu(0);
        clusterQuota.setVirtualCpuUsage(0);
        clusterQuota.setMemSizeMB(0L);
        clusterQuota.setMemSizeMBUsage(0L);
        generalQuota.setGlobalQuotaCluster(clusterQuota);

        generalQuota.setId(guid);
        generalQuota.setStoragePoolId(storagePoolUUID);
        return generalQuota;
    }
}
