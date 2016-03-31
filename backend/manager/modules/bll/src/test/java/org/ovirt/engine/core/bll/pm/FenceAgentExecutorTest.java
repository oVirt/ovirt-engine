package org.ovirt.engine.core.bll.pm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@RunWith(MockitoJUnitRunner.class)
public class FenceAgentExecutorTest {
    private static Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static Guid PROXY_HOST_ID = new Guid("44444444-4444-4444-4444-444444444444");
    private static Guid SECOND_PROXY_HOST_ID = new Guid("77777777-7777-7777-7777-777777777777");
    private static Guid FENCE_AGENT_ID = new Guid("55555555-5555-5555-5555-555555555555");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.FenceAgentMapping, ""));

    @Mock
    private VDS vds;

    @Mock
    private FenceProxyLocator proxyLocator;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private FenceAgent realAgent;

    @Mock
    AuditLogDirector auditLogDirector;

    private FenceAgentExecutor executor;

    @Before
    public void setup() {
        mockVds();
        executor = spy(new FenceAgentExecutor(vds, new FencingPolicy()));
        doReturn(resourceManager).when(executor).getResourceManager();
        doReturn(proxyLocator).when(executor).getProxyLocator();
        doReturn(auditLogDirector).when(executor).getAuditLogDirector();
        doReturn(realAgent).when(executor).createRealAgent(any(FenceAgent.class), any(VDS.class));
    }


    /**
     * Test that the return value is correct when fencing succeeds.
     */
    @Test
    public void successfulFence() {
        FenceOperationResult fenceVdsResult = new FenceOperationResult(Status.SUCCESS);
        mockFenceVdsResult(fenceVdsResult, null);
        mockProxyHost();

        FenceOperationResult result = executor.fence(FenceActionType.START, createAgent());

        assertEquals(Status.SUCCESS, result.getStatus());
        verifyAuditFenceExecutionStart(1);
    }

    /**
     * Test that fence attempt is retried with a different proxy host when first fence attempt fails
     */
    @Test
    public void successfulFenceWithDifferentProxyRetry() {
        FenceOperationResult fenceVdsResult1 = new FenceOperationResult(Status.ERROR);
        FenceOperationResult fenceVdsResult2 = new FenceOperationResult(Status.SUCCESS);
        mockFenceVdsResult(fenceVdsResult1, fenceVdsResult2);
        mockProxyHost(true);

        FenceOperationResult result = executor.fence(FenceActionType.START, createAgent());

        assertEquals(Status.SUCCESS, result.getStatus());
        verifyAuditFenceExecutionStart(2);
        verifyAuditFenceExecutionFailure(1);
    }

    /**
     * Test that fence attempt is retried with the same proxy host when first fence attempt fails and no alternative
     * proxy is found
     */
    @Test
    public void successfulFenceWithSameProxyRetry() {
        FenceOperationResult fenceVdsResult1 = new FenceOperationResult(Status.ERROR);
        FenceOperationResult fenceVdsResult2 = new FenceOperationResult(Status.SUCCESS);
        mockFenceVdsResult(fenceVdsResult1, fenceVdsResult2);
        mockProxyHost(false);

        FenceOperationResult result = executor.fence(FenceActionType.START, createAgent());

        assertEquals(Status.SUCCESS, result.getStatus());
        verifyAttemptToFindDifferentProxy();
        verifyAuditFenceExecutionStart(2);
        verifyAuditFenceExecutionFailure(1);
    }

    /**
     * Test that the whole fence execution fails when the first fence attempt fails and the second attempt using
     * a different proxy host also fails
     *
     */
    @Test
    public void failedFenceWithDifferentProxyRetry() {
        FenceOperationResult fenceVdsResult1 = new FenceOperationResult(Status.ERROR);
        FenceOperationResult fenceVdsResult2 = new FenceOperationResult(Status.ERROR);
        mockFenceVdsResult(fenceVdsResult1, fenceVdsResult2);
        mockProxyHost(true);

        FenceOperationResult result = executor.fence(FenceActionType.START, createAgent());

        assertEquals(Status.ERROR, result.getStatus());
        verifyAttemptToFindDifferentProxy();
        verifyAuditFenceExecutionFailure(2);
    }

    /**
     * Test that the whole fence execution fails when the first fence attempt fails and the second attempt using
     * the same proxy host also fails
     */
    @Test
    public void failedFenceWithSameProxyRetry() {
        FenceOperationResult fenceVdsResult1 = new FenceOperationResult(Status.ERROR);
        FenceOperationResult fenceVdsResult2 = new FenceOperationResult(Status.ERROR);
        mockFenceVdsResult(fenceVdsResult1, fenceVdsResult2);
        mockProxyHost(false);

        FenceOperationResult result = executor.fence(FenceActionType.START, createAgent());

        assertEquals(Status.ERROR, result.getStatus());
        verifyAttemptToFindDifferentProxy();
        verifyAuditFenceExecutionFailure(2);
    }

    private void mockVds() {
        when(vds.getId()).thenReturn(FENCECD_HOST_ID);
    }

    private void mockProxyHost() {
        mockProxyHost(false);
    }

    private void mockProxyHost(boolean anotherProxyAvailable) {
        VDS proxyHost = new VDS();
        proxyHost.setId(PROXY_HOST_ID);
        when(proxyLocator.findProxyHost()).thenReturn(proxyHost);
        when(proxyLocator.findProxyHost(true)).thenReturn(proxyHost);

        if (anotherProxyAvailable) {
            VDS secondProxyHost = new VDS();
            secondProxyHost.setId(SECOND_PROXY_HOST_ID);
            when(proxyLocator.findProxyHost(true, PROXY_HOST_ID)).thenReturn(secondProxyHost);
        } else {
            when(proxyLocator.findProxyHost(true, PROXY_HOST_ID)).thenReturn(null);
        }
    }

    private VDSReturnValue createVdsReturnValue(FenceOperationResult result) {
        VDSReturnValue retVal = new VDSReturnValue();
        retVal.setSucceeded(result.getStatus() != Status.ERROR);
        retVal.setReturnValue(result);
        return retVal;
    }

    private void mockFenceVdsResult(FenceOperationResult result1, FenceOperationResult result2) {
        VDSReturnValue retVal1 = createVdsReturnValue(result1);
        VDSReturnValue retVal2 = result2 == null ? null : createVdsReturnValue(result2);
        when(resourceManager.runVdsCommand(
                        eq(VDSCommandType.FenceVds),
                        any(FenceVdsVDSCommandParameters.class)))
                .thenReturn(retVal1)
                .thenReturn(retVal2);
    }

    private FenceAgent createAgent() {
        FenceAgent agent = new FenceAgent();
        agent.setId(FENCE_AGENT_ID);
        return agent;
    }

    private void verifyAttemptToFindDifferentProxy() {
        verify(proxyLocator).findProxyHost(true, PROXY_HOST_ID);
    }

    private void verifyAuditFenceExecutionStart(int expectedInvocations) {
        verify(auditLogDirector, times(expectedInvocations)).log(
                any(AuditLogableBase.class),
                eq(AuditLogType.FENCE_OPERATION_USING_AGENT_AND_PROXY_STARTED));
    }

    private void verifyAuditFenceExecutionFailure(int expectedInvocations) {
        verify(auditLogDirector, times(expectedInvocations)).log(
                any(AuditLogableBase.class),
                eq(AuditLogType.FENCE_OPERATION_USING_AGENT_AND_PROXY_FAILED));
    }
}
