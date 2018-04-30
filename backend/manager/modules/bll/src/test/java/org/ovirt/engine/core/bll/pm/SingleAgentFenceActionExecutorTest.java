package org.ovirt.engine.core.bll.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.OngoingStubbing;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class SingleAgentFenceActionExecutorTest {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.FenceStartStatusRetries, 1),
                MockConfigDescriptor.of(ConfigValues.FenceStopStatusRetries, 1),
                MockConfigDescriptor.of(ConfigValues.FenceStartStatusDelayBetweenRetriesInSec, 0),
                MockConfigDescriptor.of(ConfigValues.FenceStopStatusDelayBetweenRetriesInSec, 0)
        );
    }

    @Mock
    FenceAgentExecutor fenceAgentExecutor;

    @Mock
    FenceAgent fenceAgent;

    @Mock
    VDS fencedHost;

    private SingleAgentFenceActionExecutor executor;

    @BeforeEach
    public void setup() {
        executor = spy(new SingleAgentFenceActionExecutor(fencedHost, fenceAgent, new FencingPolicy()));
        doReturn(fenceAgentExecutor).when(executor).createAgentExecutor();
        doReturn(0).when(executor).getSleepBeforeFirstAttempt();
        doReturn(0).when(executor).getUnknownResultLimit();
        doNothing().when(executor).auditVerifyStatusRetryLimitExceeded(any());
        doReturn("host1").when(fencedHost).getHostName();
    }

    /**
     * Test successful status action
     */
    @Test
    public void successfulGetStatus() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        FenceOperationResult[] expectedResults = {
                expectedResult
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed status action
     */
    @Test
    public void failedGetStatus() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);
        FenceOperationResult[] expectedResults = {
                expectedResult
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.STATUS);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful start action
     */
    @Test
    public void successfulStart() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        FenceOperationResult[] expectedResults = {
                // result of start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                expectedResult
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful start action with 1 status retry
     */
    @Test
    public void successfulStartWithStatusRetry() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        FenceOperationResult[] expectedResults = {
                // result of start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON),
                expectedResult
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test start action with status retries exceeded
     */
    @Test
    public void failedStartStatusRetriesExceeded() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.OFF);
        FenceOperationResult[] expectedResults = {
                // result of 1st start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF),
                // result of 2nd status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF),
                // result of 2nd start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF),
                // result of 2nd status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF),
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test start action with UNKNOWN power status limit exceeded
     */
    @Test
    public void failedStartUnknownStatusLimitExceeded() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);
        FenceOperationResult[] expectedResults = {
                // result of 1st start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 2nd start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test start action with UNKNOWN power status limit exceeded on 1st attempt, but 2nd attempt is successful
     */
    @Test
    public void successfulStartUnknownStatusLimitExceededOn1stAttempt() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        FenceOperationResult[] expectedResults = {
                // result of 1st start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 2nd start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of 1st status action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON),
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test successful start action, when the 1st start attempt failed, but the 2nd one was successful
     */
    @Test
    public void successfulStartWithStartRetry() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.SUCCESS, PowerStatus.ON);
        FenceOperationResult[] expectedResults = {
                // result of the 1st start action
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN),
                // result of the 2nd start action
                new FenceOperationResult(Status.SUCCESS, PowerStatus.UNKNOWN),
                // result of status action
                expectedResult
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.START);

        validateResult(expectedResult, result);
    }

    /**
     * Test failed stop action, when the 1st start attempt failed and retrying fence for stop is not allowed
     */
    @Test
    public void failedStopWithStopRetry() {
        FenceOperationResult expectedResult =
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);
        FenceOperationResult[] expectedResults = {
                // result of the 1st stop action
                new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN),
        };
        mockFenceActionResults(expectedResults);

        FenceOperationResult result = executor.fence(FenceActionType.STOP);

        validateResult(expectedResult, result);
    }

    protected void mockFenceActionResults(FenceOperationResult[] results) {
        OngoingStubbing<FenceOperationResult> fenceMethodResult =
                when(fenceAgentExecutor.fence(any(), any()));

        for (FenceOperationResult result : results) {
            fenceMethodResult = fenceMethodResult.thenReturn(result);
        }
    }

    protected void validateResult(FenceOperationResult expected, FenceOperationResult actual) {
        assertNotNull(actual);
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPowerStatus(), actual.getPowerStatus());
    }
}
