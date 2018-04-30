package org.ovirt.engine.core.bll.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class HostFenceActionExecutorTest {
    private static Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");

    @Mock
    @InjectedMock
    public FenceAgentDao fenceAgentDao;

    @Mock
    VDS fencedHost;

    @Mock
    SingleAgentFenceActionExecutor agentExecutor1;

    @Mock
    SingleAgentFenceActionExecutor agentExecutor2;

    HostFenceActionExecutor executor;

    List<FenceAgent> fenceAgents;

    @BeforeEach
    public void setup() {
        when(fencedHost.getId()).thenReturn(FENCECD_HOST_ID);

        executor = spy(new HostFenceActionExecutor(fencedHost, new FencingPolicy()));
        doReturn(agentExecutor1).doReturn(agentExecutor2).when(executor).createFenceActionExecutor(any());
        doNothing().when(executor).alertActionSkippedAlreadyInStatus(any(), any());
        doNothing().when(executor).alertActionSkippedFencingDisabledInPolicy();

    }

    /**
     * Test successful fence action when the 1st of 2 sequential fence agents returns success
     */
    @Test
    public void successfulFenceWith1stSuccess() {
        mockFenceAgents();

        // result of fence action invoked on 1st sequential agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        // result of fence action invoked on 2nd sequential agent - set it to error, so we verify that it's not used
        mockFenceResult(
                agentExecutor2,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.ON, result.getPowerStatus());
    }

    /**
     * Test successful fence action when the 1st of 2 sequential fence agents returns error and 2nd one return success
     */
    @Test
    public void successfulFenceWith1stError2ndSuccess() {
        mockFenceAgents();

        // result of fence action invoked on 1st sequential agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        // result of fence action invoked on 2nd sequential agent
        mockFenceResult(
                agentExecutor2,
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.ON, result.getPowerStatus());
    }

    /**
     * Test failed fence action when both of 2 sequential fence agents return error
     */
    @Test
    public void failedFenceWithAllError() {
        mockFenceAgents();

        // result of fence action invoked on 1st sequential agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        // result of fence action invoked on 2nd sequential agent
        mockFenceResult(
                agentExecutor2,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        assertEquals(Status.ERROR, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test successful status action using specified fence agent
     */
    @Test
    public void successfulStatusWithSpecifiedFenceAgent() {
        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.getFenceAgentStatus(createFenceAgent(1));

        assertNotNull(result);
        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.ON, result.getPowerStatus());
    }

    /**
     * Test failed status action using specified fence agent
     */
    @Test
    public void failedStatusWithSpecifiedFenceAgent() {
        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.getFenceAgentStatus(createFenceAgent(1));

        assertNotNull(result);
        assertEquals(Status.ERROR, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test that host is powered off when status action returns power off
     */
    @Test
    public void hostIsPoweredOffWhenFenceStatusReturnsOff() {
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));

        assertTrue(executor.isHostPoweredOff());
    }

    /**
     * Test that host is not powered off when status action returns power on
     */
    @Test
    public void hostIsNotPoweredOffWhenFenceStatusReturnsOn() {
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        assertFalse(executor.isHostPoweredOff());
    }


    /**
     * Test that host is not powered off when status action returns error
     */
    @Test
    public void hostIsNotPoweredOffWhenFenceStatusFailed() {
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        assertFalse(executor.isHostPoweredOff());
    }

    /**
     * Tests {@code SingleAgentFenceActionExecutor} creation for single fence agent
     */
    @Test
    public void testSingleAgentFenceActionExecutorUsage() {
        HostFenceActionExecutor executor = new HostFenceActionExecutor(fencedHost, new FencingPolicy());

        assertTrue(
                executor.createFenceActionExecutor(createSingleAgentList(1))
                        instanceof SingleAgentFenceActionExecutor);
    }

    /**
     * Tests {@code ConcurrentAgentsFenceActionExecutor} creation for concurrent fence agents
     */
    @Test
    public void testConcurrentAgentsFenceActionExecutorUsage() {
        HostFenceActionExecutor executor = new HostFenceActionExecutor(fencedHost, new FencingPolicy());

        assertTrue(
                executor.createFenceActionExecutor(createConcurrentAgentsList(2, 1))
                        instanceof ConcurrentAgentsFenceActionExecutor);
    }

    @Test
    public void hostPowerOffSkippedWhenFenceStatusIsOff() {
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SKIPPED_ALREADY_IN_STATUS, PowerStatus.OFF));
        FenceOperationResult result = executor.fence(FenceActionType.STOP);
        assertEquals(result.getStatus(), Status.SKIPPED_ALREADY_IN_STATUS);
    }

    @Test
    public void hostPowerOnSkippedWhenFenceStatusIsOn() {
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SKIPPED_ALREADY_IN_STATUS, PowerStatus.ON));
        FenceOperationResult result = executor.fence(FenceActionType.START);
        assertEquals(result.getStatus(), Status.SKIPPED_ALREADY_IN_STATUS);
    }

    @Test
    public void hostPowerOnSkippedWhenFencingDisabledInClusterPolicy() {
        FencingPolicy fencingPolicy = new FencingPolicy();
        fencingPolicy.setFencingEnabled(false);
        executor.setFencingPolicy(fencingPolicy);
        mockFenceAgent();

        // result of fence action invoked on specified agent
        mockFenceResult(
                agentExecutor1,
                new FenceOperationResult(Status.SKIPPED_DUE_TO_POLICY, PowerStatus.OFF));
        FenceOperationResult result = executor.fence(FenceActionType.STOP);
        assertEquals(result.getStatus(), Status.SKIPPED_DUE_TO_POLICY);
    }


    protected FenceAgent createFenceAgent(int order) {
        FenceAgent agent = new FenceAgent();
        agent.setId(Guid.newGuid());
        agent.setOrder(order);
        return agent;
    }

    protected List<FenceAgent> createSingleAgentList(int order) {
        List<FenceAgent> fenceAgents = new ArrayList<>();
        fenceAgents.add(createFenceAgent(order));
        return fenceAgents;
    }

    protected List<FenceAgent> createConcurrentAgentsList(int count, int order) {
        List<FenceAgent> fenceAgents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            fenceAgents.add(createFenceAgent(order));
        }
        return fenceAgents;
    }

    protected List<FenceAgent> create2SequentialFenceAgents() {
        List<FenceAgent> fenceAgents = createSingleAgentList(1);
        fenceAgents.addAll(createSingleAgentList(2));
        return fenceAgents;
    }

    protected void mockFenceResult(FenceActionExecutor executor, FenceOperationResult result) {
        doReturn(result).when(executor).fence(any());
    }

    protected void mockFenceAgents() {
        fenceAgents = create2SequentialFenceAgents();
        when(fenceAgentDao.getFenceAgentsForHost(fencedHost.getId())).thenReturn(fenceAgents);
    }

    protected void mockFenceAgent() {
        fenceAgents = createSingleAgentList(1);
        when(fenceAgentDao.getFenceAgentsForHost(fencedHost.getId())).thenReturn(fenceAgents);
    }
}
