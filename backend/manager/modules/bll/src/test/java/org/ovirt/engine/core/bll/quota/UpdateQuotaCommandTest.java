package org.ovirt.engine.core.bll.quota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link UpdateQuotaCommand} class. */
@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateQuotaCommandTest extends BaseCommandTest {
    /** The ID of the quota used for testing */
    private static final Guid QUOTA_ID = Guid.newGuid();

    /** The parameters to test the command with */
    private QuotaCRUDParameters params = new QuotaCRUDParameters(setUpQuota(QUOTA_ID));

    /** The command to test */
    @Spy
    @InjectMocks
    private UpdateQuotaCommand command = new UpdateQuotaCommand(params, null);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.QuotaGraceStorage, 20),
                MockConfigDescriptor.of(ConfigValues.QuotaGraceCluster, 20),
                MockConfigDescriptor.of(ConfigValues.QuotaThresholdStorage, 80),
                MockConfigDescriptor.of(ConfigValues.QuotaThresholdCluster, 80)
        );
    }

    /** The quota to use for testing */
    private Quota quota;

    /** A mock of the QuotaDao used by the command */
    @Mock
    private QuotaDao quotaDao;

    @BeforeEach
    public void setUp() {
        quota = setUpQuota(QUOTA_ID);
        when(quotaDao.getById(QUOTA_ID)).thenReturn(quota);

        doNothing().when(command).removeQuotaFromCache();
        doNothing().when(command).afterUpdate();

        command.init();
    }

    private Quota setUpQuota(Guid guid) {
        Quota quota = new Quota();
        quota.setId(guid);

        int numQutoaClusters = RandomUtils.instance().nextInt(10);
        List<QuotaCluster> quotaClusters = new ArrayList<>(numQutoaClusters);
        for (int i = 0; i < numQutoaClusters; ++i) {
            quotaClusters.add(new QuotaCluster());
        }

        quota.setQuotaClusters(quotaClusters);

        int numQutoaStorages = RandomUtils.instance().nextInt(10);
        List<QuotaStorage> quotaStorages = new ArrayList<>(numQutoaStorages);
        for (int i = 0; i < numQutoaClusters; ++i) {
            quotaStorages.add(new QuotaStorage());
        }

        quota.setQuotaStorages(quotaStorages);
        return quota;
    }

    @Test
    public void testExecuteCommand() {
        // Execute the command
        command.executeCommand();

        Quota parameterQuota = command.getParameters().getQuota();
        Guid quotaId = parameterQuota.getId();
        for (QuotaStorage quotaStorage : parameterQuota.getQuotaStorages()) {
            assertNotNull(quotaStorage.getQuotaStorageId(), "Quota Storage should have been assigned an ID");
            assertEquals(quotaId, quotaStorage.getQuotaId(), "Wrong Qutoa ID on Quota Storage");
        }

        for (QuotaCluster quotaCluster : parameterQuota.getQuotaClusters()) {
            assertNotNull(quotaCluster.getQuotaClusterId(), "Quota Cluster should have been assigned an ID");
            assertEquals(quotaId, quotaCluster.getQuotaId(), "Wrong Qutoa ID on Quota Cluster");
        }

        // Verify the quota was updated in the database
        verify(quotaDao).update(parameterQuota);

        // Assert the return value
        assertTrue(command.getReturnValue().getSucceeded(), "Execution should be successful");
    }

    @Test
    public void testFailToUpdateDefaultQuota() {
        quota.setDefault(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_QUOTA_DEFAULT_CANNOT_BE_CHANGED);
    }

    @Test
    public void testChangeToDefaultQuota() {
        params.getQuota().setDefault(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
    }

}
