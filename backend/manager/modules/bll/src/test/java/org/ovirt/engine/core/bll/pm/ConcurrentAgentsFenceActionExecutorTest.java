package org.ovirt.engine.core.bll.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
import org.ovirt.engine.core.utils.ExecutorServiceExtension;

@ExtendWith({MockitoExtension.class, ExecutorServiceExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConcurrentAgentsFenceActionExecutorTest {
    @Mock
    SingleAgentFenceActionExecutor singleExecutor1;

    @Mock
    SingleAgentFenceActionExecutor singleExecutor2;

    @Mock
    VDS fencedHost;

    FenceAgent agent1;

    FenceAgent agent2;

    ConcurrentAgentsFenceActionExecutor executor;

    @BeforeEach
    public void setup() {
        agent1 = new FenceAgent();
        agent1.setId(Guid.newGuid());
        agent2 = new FenceAgent();
        agent2.setId(Guid.newGuid());

        List<FenceAgent> fenceAgents = new ArrayList<>();
        fenceAgents.add(agent1);
        fenceAgents.add(agent2);

        executor = spy(new ConcurrentAgentsFenceActionExecutor(
                fencedHost,
                fenceAgents,
                new FencingPolicy()));

        doReturn(singleExecutor1).when(executor).createFenceActionExecutor(eq(agent1));
        doReturn(singleExecutor2).when(executor).createFenceActionExecutor(eq(agent2));
    }

    /**
     * Test status action with power on result, when both agents reports power on
     */
    @Test
    public void statusOnWhenAllReportsOn() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test status action with power on result, when one agent reports power on
     */
    @Test
    public void statusOnWhenOneReportsOn() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test status action with power on result, when one agent reports power on and other reports error
     */
    @Test
    public void statusOnWhenOneReportsOnAndOtherFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test status action with power off result, when all agents reports power off
     */
    @Test
    public void statusOffWhenAllReportsOff() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed status action, when one agent reports power off and other reports error
     */
    @Test
    public void failedStatusWhenOneReportsOffAndOtherFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed status action, when all agents failed to get status
     */
    @Test
    public void failedStatusWhenAllAgentsFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful start action, when all agents were successful
     */
    @Test
    public void successfulStartWhenAllAgentsSuccessful() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful start action, when one agent was successful and another failed
     */
    @Test
    public void successfulStartWhenOneSuccessfulAndAnotherFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.ON));

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed start action, when all agents failed
     */
    @Test
    public void failedStartWhenAllAgentsFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful stop action, when all agents were successful
     */
    @Test
    public void successfulStopWhenAllAgentsSuccessful() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));

        FenceOperationResult result = executor.fence(FenceActionType.STOP);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed stop action, when one agent was successful and another failed
     */
    @Test
    public void failedStopWhenOneSuccessfulAndAnotherFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF));

        FenceOperationResult result = executor.fence(FenceActionType.STOP);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed stop action, when all agents failed
     */
    @Test
    public void failedStopWhenAllAgentsFailed() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);

        mockSingleAgentResult(singleExecutor1, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));
        mockSingleAgentResult(singleExecutor2, new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN));

        FenceOperationResult result = executor.fence(FenceActionType.STOP);

        validateResult(expectedResult, result);
    }

    protected void mockSingleAgentResult(SingleAgentFenceActionExecutor executor, FenceOperationResult result) {
        doReturn(result).when(executor).fence(any());
    }

    protected void validateResult(FenceOperationResult expected, FenceOperationResult actual) {
        assertNotNull(actual);
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPowerStatus(), actual.getPowerStatus());
    }
}
