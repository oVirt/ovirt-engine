package org.ovirt.engine.core.bll.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class HostFenceActionExecutorTest {
    @Mock
    VDS fencedHost;

    @Mock
    SingleAgentFenceActionExecutor agentExecutor1;

    @Mock
    SingleAgentFenceActionExecutor agentExecutor2;

    HostFenceActionExecutor executor;

    List<FenceAgent> fenceAgents;

    @Before
    public void setup() {
        doReturn("host1").when(fencedHost).getHostName();

        executor = spy(new HostFenceActionExecutor(fencedHost, new FencingPolicy()));
        doReturn(agentExecutor1)
                .doReturn(agentExecutor2)
                .when(executor).createFenceActionExecutor(any(List.class));
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
        doReturn(result).when(executor).fence(any(FenceActionType.class));
    }

    protected void mockFenceAgents() {
        fenceAgents = create2SequentialFenceAgents();
        doReturn(fenceAgents).when(fencedHost).getFenceAgents();
    }

    protected void mockFenceAgent() {
        fenceAgents = createSingleAgentList(1);
        doReturn(fenceAgents).when(fencedHost).getFenceAgents();
    }
}
