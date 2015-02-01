package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class StartVdsCommandTest extends DbDependentTestBase {

    private static final String HOST_NAME = "HostName";
    private static final Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static final Guid FENCECD_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.FenceStartStatusRetries, 2),
                    MockConfigRule.mockConfig(ConfigValues.FenceStartStatusDelayBetweenRetriesInSec, 1));
    @Mock
    private FenceExecutor executor;
    private FenceAgent agent1;
    private FenceAgent agent2;
    private DbFacade dbFacade;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private VmDAO vmDao;
    @Mock
    private AuditLogDAO auditLogDao;
    @Mock
    private VdsGroupDAO vdsGroupDao;
    @Mock
    private BackendInternal backend;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    private StartVdsCommand<FenceVdsActionParameters> command;

    @Before
    public void setup() {
        dbFacade = DbFacade.getInstance();
        initAgents();
        mockDbFacades();
        initCommand();
        mockBackend();
    }

    private void mockDbFacades() {
        mockVdsGroupDao();
        mockVmDao();
        mockAuditLogDao();
        mockVdsDao();
    }

    private void mockVdsGroupDao() {
        when(dbFacade.getVdsGroupDao()).thenReturn(vdsGroupDao);
        VDSGroup cluster = new VDSGroup();
        cluster.setId(FENCECD_HOST_CLUSTER_ID);
        when(vdsGroupDao.get(FENCECD_HOST_CLUSTER_ID)).thenReturn(cluster);
    }

    private void mockVmDao() {
        when(vmDao.getAllRunningForVds(eq(FENCECD_HOST_ID))).thenReturn(new LinkedList<VM>());
        when(dbFacade.getVmDao()).thenReturn(vmDao);
    }

    private void mockAuditLogDao() {
        doNothing().when(auditLogDao).save(any(AuditLog.class));
        when(dbFacade.getAuditLogDao()).thenReturn(auditLogDao);
    }

    private void mockVdsDao() {
        VDS vds = createHost();
        when(vdsDao.get(FENCECD_HOST_ID)).thenReturn(vds);
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);
    }

    private void initAgents() {
        agent1 = new FenceAgent();
        agent1.setOrder(1);
        agent2 = new FenceAgent();
        agent2.setOrder(2);
    }

    private void mockBackend() {
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
        doReturn(backend).when(command).getBackend();
    }

    private void initCommand() {
        FenceVdsActionParameters params = new FenceVdsActionParameters();
        params.setVdsId(FENCECD_HOST_ID);
        command = new StartVdsCommand<>(params);
        command.setFenceExecutor(executor);
        command = spy(command);
        stub(command.getSleepBeforeFirstAttempt()).toReturn(0);
        command.setVdsGroupId(FENCECD_HOST_CLUSTER_ID);
    }

    private VDS createHost() {
        VDS vds = new VDS();
        vds.setId(FENCECD_HOST_ID);
        vds.setVdsGroupId(FENCECD_HOST_CLUSTER_ID);
        vds.setVdsName(HOST_NAME);
        vds.setStatus(VDSStatus.Up);
        List<FenceAgent> agents = new LinkedList<>();
        agents.add(agent1);
        agents.add(agent2);
        vds.setFenceAgents(agents);
        return vds;
    }

    private void mockCheckStatus(String status) {
        VDSFenceReturnValue retValue = new VDSFenceReturnValue();
        retValue.setSucceeded(true);
        FenceStatusReturnValue stat = new FenceStatusReturnValue(status, "");
        retValue.setReturnValue(stat);
        when(executor.checkHostStatus()).thenReturn(retValue);
    }

    private VDSFenceReturnValue createStatus(String value) {
        VDSFenceReturnValue retValue = new VDSFenceReturnValue();
        retValue.setSucceeded(true);
        FenceStatusReturnValue stat = new FenceStatusReturnValue(value, "");
        retValue.setReturnValue(stat);
        return retValue;
    }

    private void mockVdsSingleAgent() {
        VDS vds = createHost();
        vds.getFenceAgents().remove(1); // remove the second agent
        when(vdsDao.get(FENCECD_HOST_ID)).thenReturn(vds);
    }

    private void mockExecutor(FenceAgent agent, boolean success) {
        VDSFenceReturnValue returnValue = new VDSFenceReturnValue();
        returnValue.setSucceeded(success);
        returnValue.setFenceAgentUsed(agent);
        when(executor.fence(eq(FenceActionType.Start), eq(agent))).thenReturn(returnValue);
    }

    /**
     * This test verifies that if the fence operation fails, the initial status is restored (re-set). Without necessary
     * mocking, VdcBLLException is thrown and the command fails. In the finally() clause, SetVdsStatus is invoked to
     * restore the old status. This test verifies that this invocation was made.
     */
    @Test()
    public void onFailureResetInitialStatus() {
        try {
            command.executeCommand();
        } catch (VdcBLLException exception) {
            verify(vdsBrokerFrontend).RunVdsCommand(eq(VDSCommandType.SetVdsStatus),
                    any(SetVdsStatusVDSCommandParameters.class));
            return;
        }
        fail();
    }

    /**
     * This test verifies that when the fence operation is successful, it is not attempted again with the next agent
     */
    @Test
    public void onSuccessDontTryNextAgent() {
        mockExecutor(agent1, true);
        mockCheckStatus("on");
        // this won't happen, because second agent isn't reached.
        when(executor.fence(eq(FenceActionType.Start), eq(agent2))).thenThrow(new IllegalStateException());
        command.executeCommand();
    }

    /**
     * This test verifies that when the fence operation fails using the first agent, the second agent is used.
     */
    @Test
    public void onFailureTryNextAgent() {
        mockExecutor(agent1, false);
        mockExecutor(agent2, true);
        mockCheckStatus("on");
        command.executeCommand();
        assertTrue(command.getSucceeded());
    }

    /**
     * This test verifies that when the fence operation is successful, the return value contains all that is should:
     * succeeded=true, the agents used, and the object returned.
     */
    @Test
    public void onSuccessReturnValueOk() {
        mockExecutor(agent1, true);
        mockCheckStatus("on");
        command.executeCommand();
        assertTrue(command.getSucceeded());
        Object commandReturnValue = command.getActionReturnValue();
        assertNotNull(commandReturnValue);
        assertTrue(commandReturnValue instanceof VDSFenceReturnValue);
        VDSFenceReturnValue commandReturnValueCasted = (VDSFenceReturnValue)commandReturnValue;
        assertEquals(commandReturnValueCasted.getFenceAgentUsed(), agent1);
    }

    /**
     * This test verifies that auditing is done. Due to a difficulty to set an expectation for a specific AuditLog
     * object (equals() compares too many fields, it's complicated to accurately set the expectation), the test counts
     * the times that an audit message is saved to the database. In case of success, there are 2 audit messages, one
     * upon entering the command, and the other upon completion, to report success.
     */
    @Test
    public void onSuccessAudit() {
        mockExecutor(agent1, true);
        mockCheckStatus("on");
        command.executeCommand();
        verify(auditLogDao, times(2)).save(any(AuditLog.class));
    }

    /**
     * This test verifies that when the fence operation fails, not due to a fencing-policy restriction, an alert is
     * shown. Due to a difficulty to set an expectation for a specific AuditLog object (equals() compares too many
     * fields, it's complicated to accurately set the expectation), the test counts the times that an audit message is
     * saved to the database. In case of success, there are 2 audit messages. In case of failure the second audit
     * message isn't reached, but because of the auditing of the alert, there *are still* 2 audit messages.
     */
    @Test()
    public void onFaliureAlertShown() {
        mockVdsSingleAgent();
        mockExecutor(agent1, false);
        mockCheckStatus("on");
        try {
            command.executeCommand();
        } catch (VdcBLLException exception) {
            verify(auditLogDao, times(2)).save(any(AuditLog.class));
            return;
        }
        fail();
    }

    /**
     * After fence-operation is performed, the command waits for the desired status to be reached. This test verifies
     * that wait-for-status is retried according to the number of retries specified.
     */
    @Test
    public void onFailureRetryWaitForStatus() {
        mockVdsSingleAgent();
        mockExecutor(agent1, true);
        when(executor.checkHostStatus()).thenReturn(createStatus("off")).thenReturn(createStatus("on"));
        command.executeCommand();
        assertTrue(command.getSucceeded());
    }
}
