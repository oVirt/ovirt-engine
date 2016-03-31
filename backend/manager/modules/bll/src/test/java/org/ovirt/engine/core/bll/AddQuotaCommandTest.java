package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

public class AddQuotaCommandTest extends BaseCommandTest {
    @Mock
    private QuotaDao quotaDao;

    /**
     * The command under test.
     */
    private AddQuotaCommand command;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.QuotaGraceStorage, 20),
            mockConfig(ConfigValues.QuotaGraceCluster, 20),
            mockConfig(ConfigValues.QuotaThresholdStorage, 80),
            mockConfig(ConfigValues.QuotaThresholdCluster, 80)
            );

    @Before
    public void testSetup() {
        mockQuotaDao();
    }

    private void mockQuotaDao() {
        when(quotaDao.getById(any(Guid.class))).thenReturn(mockGeneralStorageQuota());
    }

    @Test
    public void testExecuteCommand() throws Exception {
        AddQuotaCommand addQuotaCommand = createCommand();
        addQuotaCommand.executeCommand();
    }

    @Test
    public void testValidateCommand() throws Exception {
        AddQuotaCommand addQuotaCommand = createCommand();
        addQuotaCommand.validate();
    }

    private AddQuotaCommand createCommand() {
        QuotaCRUDParameters param = new QuotaCRUDParameters(mockGeneralStorageQuota());
        command = spy(new AddQuotaCommand(param, null));
        doReturn(quotaDao).when(command).getQuotaDao();

        return command;
    }

    private Quota mockGeneralStorageQuota() {
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

        generalQuota.setId(Guid.newGuid());
        generalQuota.setStoragePoolId(Guid.newGuid());
        return generalQuota;
    }
}
