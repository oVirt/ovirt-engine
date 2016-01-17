package org.ovirt.engine.core.bll.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;

@RunWith(MockitoJUnitRunner.class)
public class StartVdsCommandTest extends DbDependentTestBase {

    private static final String HOST_NAME = "HostName";
    private static final Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static final Guid FENCECD_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");

    @Mock
    private HostFenceActionExecutor executor;
    private FenceAgent agent1;
    private FenceAgent agent2;
    private DbFacade dbFacade;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private AuditLogDao auditLogDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private BackendInternal backend;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private AuditLogDirector auditLogDirector;

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
        mockClusterDao();
        mockVmDao();
        mockAuditLogDao();
        mockVdsDao();
        mockVdsDynamicDao();
    }

    private void mockClusterDao() {
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        Cluster cluster = new Cluster();
        cluster.setId(FENCECD_HOST_CLUSTER_ID);
        when(clusterDao.get(FENCECD_HOST_CLUSTER_ID)).thenReturn(cluster);
    }

    private void mockVmDao() {
        when(vmDao.getAllRunningForVds(eq(FENCECD_HOST_ID))).thenReturn(new LinkedList<>());
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

    private void mockVdsDynamicDao() {
        VdsDynamic currentVds = new VdsDynamic();
        currentVds.setId(FENCECD_HOST_ID);
        currentVds.setStatus(VDSStatus.NonResponsive);
        when(vdsDynamicDao.get(FENCECD_HOST_ID)).thenReturn(currentVds);
        when(dbFacade.getVdsDynamicDao()).thenReturn(vdsDynamicDao);
    }

    private void initAgents() {
        agent1 = new FenceAgent();
        agent1.setOrder(1);
        agent2 = new FenceAgent();
        agent2.setOrder(2);
    }

    private void mockBackend() {
        doReturn(backend).when(command).getBackend();
    }

    private void initCommand() {
        FenceVdsActionParameters params = new FenceVdsActionParameters();
        params.setVdsId(FENCECD_HOST_ID);
        command = new StartVdsCommand<>(params, null);
        command.setAuditLogDirector(auditLogDirector);
        command = spy(command);
        doReturn(executor).when(command).createHostFenceActionExecutor(any(VDS.class), any(FencingPolicy.class));
        doReturn(vdsBrokerFrontend).when(command).getVdsBroker();
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
        doReturn(result).when(executor).fence(any(FenceActionType.class));
    }

    /**
     * This test verifies that if the fence operation fails, the initial status is restored (re-set). Without necessary
     * mocking, EngineException is thrown and the command fails. In the finally() clause, SetVdsStatus is invoked to
     * restore the old status. This test verifies that this invocation was made.
     */
    @Test()
    public void onFailureResetInitialStatus() {
        mockExecutor(false);
        try {
            command.executeCommand();
        } catch (EngineException exception) {
            verify(vdsBrokerFrontend).runVdsCommand(eq(VDSCommandType.SetVdsStatus),
                    any(SetVdsStatusVDSCommandParameters.class));
            return;
        }
        fail();
    }

    /**
     * This test verifies that when the fence operation is successful, the return value contains all that is should:
     * succeeded=true, the agents used, and the object returned.
     */
    @Test
    public void onSuccessReturnValueOk() {
        mockExecutor(true);
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
        command.executeCommand();
        verify(auditLogDirector, times(2)).log(any(AuditLogableBase.class), any(AuditLogType.class));

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
        try {
            command.executeCommand();
            fail();
        } catch (EngineException ex) {
            verify(auditLogDirector, times(3)).log(any(AuditLogableBase.class), any(AuditLogType.class));
        }
    }
}
