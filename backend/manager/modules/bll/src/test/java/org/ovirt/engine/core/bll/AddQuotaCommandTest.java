package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.utils.MockConfigRule;

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
            mockConfig(ConfigValues.QuotaGraceVdsGroup, 20),
            mockConfig(ConfigValues.QuotaThresholdStorage, 80),
            mockConfig(ConfigValues.QuotaThresholdVdsGroup, 80)
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
    public void testCanDoActionCommand() throws Exception {
        AddQuotaCommand addQuotaCommand = createCommand();
        addQuotaCommand.canDoAction();
    }

    private AddQuotaCommand createCommand() {
        QuotaCRUDParameters param = new QuotaCRUDParameters(mockGeneralStorageQuota());
        command = spy(new AddQuotaCommand(param));
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

        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(0);
        vdsGroupQuota.setVirtualCpuUsage(0);
        vdsGroupQuota.setMemSizeMB(0L);
        vdsGroupQuota.setMemSizeMBUsage(0L);
        generalQuota.setGlobalQuotaVdsGroup(vdsGroupQuota);

        generalQuota.setId(Guid.newGuid());
        generalQuota.setStoragePoolId(Guid.newGuid());
        return generalQuota;
    }
}
