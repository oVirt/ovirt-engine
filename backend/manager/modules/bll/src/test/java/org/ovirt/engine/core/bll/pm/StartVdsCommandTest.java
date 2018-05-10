package org.ovirt.engine.core.bll.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;

public class StartVdsCommandTest extends BaseCommandTest {

    private static final String HOST_NAME = "HostName";
    private static final Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static final Guid FENCECD_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");

    @Mock
    private HostFenceActionExecutor executor;
    private FenceAgent agent1;
    private FenceAgent agent2;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Spy
    @InjectMocks
    private StartVdsCommand<FenceVdsActionParameters> command =
            new StartVdsCommand<>(new FenceVdsActionParameters(FENCECD_HOST_ID), null);

    @BeforeEach
    public void setup() {
        initAgents();
        mockDbFacades();
        initCommand();
    }

    private void mockDbFacades() {
        mockVdsDao();
        mockVdsDynamicDao();
    }

    private void mockVdsDao() {
        VDS vds = createHost();
        when(vdsDao.get(FENCECD_HOST_ID)).thenReturn(vds);
    }

    private void mockVdsDynamicDao() {
        VdsDynamic currentVds = new VdsDynamic();
        currentVds.setId(FENCECD_HOST_ID);
        currentVds.setStatus(VDSStatus.Reboot);
        when(vdsDynamicDao.get(FENCECD_HOST_ID)).thenReturn(currentVds);
    }

    private void initAgents() {
        agent1 = new FenceAgent();
        agent1.setOrder(1);
        agent2 = new FenceAgent();
        agent2.setOrder(2);
    }

    private void initCommand() {
        doReturn(executor).when(command).createHostFenceActionExecutor(any(), any());
        command.setClusterId(FENCECD_HOST_CLUSTER_ID);
    }

    private VDS createHost() {
        VDS vds = new VDS();
        vds.setId(FENCECD_HOST_ID);
        vds.setClusterId(FENCECD_HOST_CLUSTER_ID);
        vds.setVdsName(HOST_NAME);
        vds.setStatus(VDSStatus.Up);
        List<FenceAgent> agents = new LinkedList<>();
        agents.add(agent1);
        agents.add(agent2);
        vds.setFenceAgents(agents);
        return vds;
    }

    private void mockExecutor(boolean success) {
        FenceOperationResult result;
        if (success) {
            result = new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        } else {
            result = new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);
        }
        doReturn(result).when(executor).fence(any());
    }

    /**
     * This test verifies that if the fence operation fails, the initial status is restored (re-set). Without necessary
     * mocking, EngineException is thrown and the command fails. In the finally() clause, SetVdsStatus is invoked to
     * restore the old status. This test verifies that this invocation was made.
     */
    @Test
    public void onFailureResetInitialStatus() {
        mockExecutor(false);
        assertThrows(EngineException.class, command::executeCommand);
        verify(vdsBrokerFrontend).runVdsCommand(eq(VDSCommandType.SetVdsStatus), any());
    }

    /**
     * This test verifies that when the fence operation is successful, the return value contains all that is should:
     * succeeded=true, the agents used, and the object returned.
     */
    @Test
    public void onSuccessReturnValueOk() {
        mockExecutor(true);
        doNothing().when(command).teardown();
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
        Object commandReturnValue = command.getReturnValue().getActionReturnValue();
        assertNotNull(commandReturnValue);
        assertTrue(commandReturnValue instanceof FenceOperationResult);
        FenceOperationResult commandReturnValueCasted = (FenceOperationResult) commandReturnValue;
        assertEquals(Status.SUCCESS, commandReturnValueCasted.getStatus());
    }

    /**
     * This test verifies that auditing is done. Due to a difficulty to set an expectation for a specific AuditLog
     * object (equals() compares too many fields, it's complicated to accurately set the expectation), the test counts
     * the times that an audit message is saved to the database. In case of success, there are 2 audit messages, one
     * upon entering the command, and the other upon completion, to report success.
     */
    @Test
    public void onSuccessAudit() {
        mockExecutor(true);
        doNothing().when(command).teardown();
        command.executeCommand();
        verify(auditLogDirector, times(2)).log(any(), any());

    }

    /**
     * This test verifies that when the fence operation fails, not due to a fencing-policy restriction, an alert is
     * shown. Due to a difficulty to set an expectation for a specific AuditLog object (equals() compares too many
     * fields, it's complicated to accurately set the expectation), the test counts the times that an audit message is
     * saved to the database. In case of success, there are 2 audit messages. In case of failure the second audit
     * message isn't reached, but because of the auditing of the alert, there *are still* 2 audit messages.
     */
    @Test
    public void onFailureAlertShown() {
        mockExecutor(false);
        assertThrows(EngineException.class, command::executeCommand);
        verify(auditLogDirector, times(3)).log(any(), any());
    }
}
