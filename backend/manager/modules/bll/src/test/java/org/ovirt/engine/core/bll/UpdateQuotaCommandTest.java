package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link UpdateQuotaCommand} class. */
public class UpdateQuotaCommandTest extends BaseCommandTest {
    /** The command to test */
    private UpdateQuotaCommand command;

    /** The parameters to test the command with */
    private QuotaCRUDParameters params;

    /** The quota to use for testing */
    private Quota quota;

    /** A mock of the QuotaDao used by the command */
    @Mock
    private QuotaDao quotaDao;

    @Before
    public void setUp() {
        setUpQuota();
        params = new QuotaCRUDParameters(quota);
        command = spy(new UpdateQuotaCommand(params, null));
        doReturn(quotaDao).when(command).getQuotaDao();
        doNothing().when(command).removeQuotaFromCache();
        doNothing().when(command).afterUpdate();
    }

    private void setUpQuota() {
        quota = new Quota();
        quota.setId(Guid.newGuid());

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
    }

    @Test
    public void testExecuteCommand() {
        // Execute the command
        command.executeCommand();

        Guid quotaId = quota.getId();
        for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
            assertNotNull("Quota Storage should have been assigned an ID", quotaStorage.getQuotaStorageId());
            assertEquals("Wrong Qutoa ID on Quota Storage", quotaId, quotaStorage.getQuotaId());
        }

        for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {
            assertNotNull("Quota Cluster should have been assigned an ID", quotaCluster.getQuotaClusterId());
            assertEquals("Wrong Qutoa ID on Quota Cluster", quotaId, quotaCluster.getQuotaId());
        }

        // Verify the quota was updated in the database
        verify(quotaDao).update(quota);

        // Assert the return value
        assertTrue("Execution should be successful", command.getReturnValue().getSucceeded());
    }
}
