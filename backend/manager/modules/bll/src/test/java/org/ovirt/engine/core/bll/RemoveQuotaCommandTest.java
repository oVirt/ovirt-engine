package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

public class RemoveQuotaCommandTest extends BaseCommandTest {

    private final Guid quotaGuid = Guid.newGuid();
    private final Guid storagePoolUUID = Guid.newGuid();
    private Quota quota;

    @Mock
    private QuotaDao quotaDao;

    @Mock
    private QuotaManager quotaManager;

    /**
     * The command under test.
     */
    @InjectMocks
    private RemoveQuotaCommand command = createCommand();

    @Before
    public void mockQuotaDao() {
        quota = mockStorageQuota(quotaGuid);
        when(quotaDao.getById(quotaGuid)).thenReturn(quota);
        List<Quota> quotaList = new ArrayList<>();
        quotaList.add(new Quota());
        quotaList.add(new Quota());
        when(quotaDao.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(quotaList);
        when(quotaDao.isQuotaInUse(any(Quota.class))).thenReturn(false);
    }

    @Test
    public void testExecuteCommand() throws Exception {
        command.executeCommand();
        assertTrue(command.getSucceeded());
    }

    @Test
    public void testValidateCommand() throws Exception {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testFailToRemoveDefaultQuota() {
        quota.setDefault(true);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_QUOTA_DEFAULT_CANNOT_BE_CHANGED);
    }

    private RemoveQuotaCommand createCommand() {
        IdParameters param = new IdParameters(quotaGuid);
        return new RemoveQuotaCommand(param, null);
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
