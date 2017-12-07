package org.ovirt.engine.core.bll.quota;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class AddQuotaCommandTest extends BaseCommandTest {
    @Mock
    private QuotaDao quotaDao;

    /**
     * The command under test.
     */
    @InjectMocks
    private AddQuotaCommand command = createCommand();

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.QuotaGraceStorage, 20),
                MockConfigDescriptor.of(ConfigValues.QuotaGraceCluster, 20),
                MockConfigDescriptor.of(ConfigValues.QuotaThresholdStorage, 80),
                MockConfigDescriptor.of(ConfigValues.QuotaThresholdCluster, 80)
        );
    }

    @BeforeEach
    public void testSetup() {
        command.init();
    }

    @Test
    public void testExecuteCommand() {
        command.executeCommand();
    }

    @Test
    public void testValidateCommand() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testAddDefaultQuota() {
        command.getParameters().getQuota().setDefault(true);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
    }

    private AddQuotaCommand createCommand() {
        QuotaCRUDParameters param = new QuotaCRUDParameters(mockGeneralStorageQuota());
        return new AddQuotaCommand(param, null);
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
