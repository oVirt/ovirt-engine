package org.ovirt.engine.core.bll.quota;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
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

    @BeforeEach
    public void mockQuotaDao() {
        quota = mockStorageQuota(quotaGuid);
        when(quotaDao.getById(quotaGuid)).thenReturn(quota);
    }

    @Test
    public void testExecuteCommand() {
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testValidateCommand() {
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
