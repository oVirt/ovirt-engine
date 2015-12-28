package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;

@RunWith(MockitoJUnitRunner.class)
public class FenceVdsVDSCommandTest {
    private static Guid TARGET_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static Guid PROXY_HOST_ID = new Guid("44444444-4444-4444-4444-444444444444");
    private static Guid DC_ID = new Guid("22222222-2222-2222-2222-222222222222");

    private FenceVdsVDSCommand<FenceVdsVDSCommandParameters> command;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private VDS targetHost;

    @Mock
    private VdsStatic targetVdsStatic;

    @Mock
    private VDS proxyHost;

    @Mock
    private IVdsServer broker;

    @Before
    public void setupMocks() {
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);

        when(vdsDao.get(eq(TARGET_HOST_ID))).thenReturn(targetHost);
        when(targetHost.getId()).thenReturn(TARGET_HOST_ID);

        when(vdsDao.get(eq(PROXY_HOST_ID))).thenReturn(proxyHost);
        when(proxyHost.getId()).thenReturn(PROXY_HOST_ID);
        when(proxyHost.getClusterCompatibilityVersion()).thenReturn(Version.getLast());
    }

    private void setupCommand(FenceVdsVDSCommandParameters params) {
        command = new FenceVdsVDSCommand<FenceVdsVDSCommandParameters>(params) {

            @Override
            protected IVdsServer initializeVdsBroker(Guid vdsId) {
                return broker;
            }

            @Override
            protected DbFacade getDbFacade() {
                return dbFacade;
            }

            @Override
            protected VdsStatic getAndSetVdsStatic() {
                return targetVdsStatic;
            }

            @Override
            protected void alertPowerManagementStatusFailed(String reason) {
            }

            @Override
            protected void alertActionSkippedAlreadyInStatus() {
            }
        };
    }

    private Map<String, Object> createBrokerResultMap(
            int returnCode,
            String message,
            String powerStatus,
            String operationStatus) {

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("code", returnCode);
        statusMap.put("message", message);

        Map<String, Object> map = new HashMap<>();
        map.put("status", statusMap);
        map.put("power", powerStatus);
        map.put("operationStatus", operationStatus);
        return map;
    }

    private void setupBrokerResult(Map<String, Object> first) {
        setupBrokerResult(first, null);
    }

    private void setupBrokerResult(Map<String, Object> first, Map<String, Object> second) {
        when(broker.fenceNode(
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(String.class),
                        any(Map.class)))
                .thenReturn(new FenceStatusReturnForXmlRpc(first))
                .thenReturn(second == null ? null : new FenceStatusReturnForXmlRpc(second));
    }

    private FenceVdsVDSCommandParameters setupCommandParams(FenceActionType fenceAction) {
        FenceAgent agent = new FenceAgent();
        agent.setIp("1.2.3.4");
        agent.setPort(1234);
        agent.setType("ipmilan");
        agent.setUser("admin");
        agent.setPassword("admin");
        agent.setOptions("");
        return new FenceVdsVDSCommandParameters(
                PROXY_HOST_ID,
                TARGET_HOST_ID,
                agent,
                fenceAction,
                null);
    }

    /**
     * Tests result of successful get power status of host
     */
    @Test
    public void successfulGetStatus() {
        String agentMessage = "Test succeeded: on";

        setupCommand(setupCommandParams(FenceActionType.STATUS));
        setupBrokerResult(createBrokerResultMap(0, agentMessage, "on", null));

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.ON, result.getPowerStatus());
        assertEquals(agentMessage, result.getMessage());
    }

    /**
     * Tests result of failed get power status of host
     */
    @Test
    public void failedGetStatus() {
        String agentMessage = "Test failed, status unknown";

        setupCommand(setupCommandParams(FenceActionType.STATUS));
        setupBrokerResult(createBrokerResultMap(1, agentMessage, "unknown", null));

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.ERROR, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
        assertEquals(agentMessage, result.getMessage());
    }

    /**
     * Test execution of stop action when host is powered down
     */
    @Test
    public void stopHostWhichIsPoweredDown() {
        setupCommand(setupCommandParams(FenceActionType.STOP));
        setupBrokerResult(
                createBrokerResultMap(0, "", "off", null)); // result of STATUS action executed prior to STOP action

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.SKIPPED_ALREADY_IN_STATUS, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test execution of stop action when host is powered up
     */
    @Test
    public void stopHostWhichIsPoweredUp() {
        setupCommand(setupCommandParams(FenceActionType.STOP));
        setupBrokerResult(
                createBrokerResultMap(0, "", "on", null), // result of STATUS action executed prior to STOP action
                createBrokerResultMap(0, "", "unknown", "initiated")); // result of actual STOP action

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test execution of stop action when host power status cannot be determined
     */
    @Test
    public void stopHostWithUnknownPowerStatus() {
        setupCommand(setupCommandParams(FenceActionType.STOP));
        setupBrokerResult(
                createBrokerResultMap(1, "", "unknown", null), // result of STATUS action executed prior to STOP action
                createBrokerResultMap(0, "", "unknown", "initiated")); // result of actual STOP action

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test execution of stop action when fencing policy forbids stopping host still connected to storage, so stopping
     * the host is skipped
     */
    @Test
    public void stopHostSkippedDueToFencingPolicy() {
        setupCommand(setupCommandParams(FenceActionType.STOP));
        setupBrokerResult(
                createBrokerResultMap(0, "", "on", null), // result of STATUS action executed prior to STOP action
                createBrokerResultMap(0, "", "unknown", "skipped")); // result of actual STOP action

        command.execute();
        FenceOperationResult result = (FenceOperationResult) command.getVDSReturnValue().getReturnValue();

        assertEquals(Status.SKIPPED_DUE_TO_POLICY, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }
}
