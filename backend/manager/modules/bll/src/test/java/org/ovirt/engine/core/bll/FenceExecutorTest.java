package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.common.config.ConfigValues;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class FenceExecutorTest {

    private static final Version CLUSTER_VERSION = Version.v3_0;
    private static final ArchitectureType CLUSTER_ARCHITECTURE_TYPE = ArchitectureType.ppc64;
    private static Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static Guid PROXY_HOST_ID = new Guid("44444444-4444-4444-4444-444444444444");
    private static Guid SECOND_PROXY_HOST_ID = new Guid("77777777-7777-7777-7777-777777777777");
    private static Guid FENCECD_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");
    private static Guid FENCED_HOST_DATACENTER_ID = new Guid("33333333-3333-3333-3333-333333333333");
    private static Guid FENCE_AGENT_ID = new Guid("55555555-5555-5555-5555-555555555555");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.FenceAgentMapping, ""));

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VDS vds;

    @Mock
    private VdsDAO vdsDao;

    @Mock
    private VdsGroupDAO vdsGroupDao;

    @Mock
    private AuditLogDAO auditLogDao;

    private VdsStatic vdsStatic = new VdsStatic();

    @Mock
    private VdsArchitectureHelper architectureHelper;

    @Mock
    private FenceProxyLocator proxyLocator;

    private FenceExecutor executor;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private BackendInternal backend;

    @Before
    public void setup() {
        mockDbFacades();
        mockVds();
        executor = new FenceExecutor(vds);
        executor.setArchitectureHelper(architectureHelper);
        executor.setProxyLocator(proxyLocator);
        executor = spy(executor);
        stub(executor.getBackend()).toReturn(backend);
        VDSReturnValue retValue = new VDSReturnValue();
        retValue.setSucceeded(true);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.FenceVds), any(VDSParametersBase.class))).thenReturn(retValue);
        when(architectureHelper.getArchitecture(vdsStatic)).thenReturn(CLUSTER_ARCHITECTURE_TYPE);
    }

    private void mockDbFacades() {
        when(vdsDao.get(FENCECD_HOST_ID)).thenReturn(vds);
        VDSGroup cluster = mockCluster();
        when(vdsGroupDao.get(FENCECD_HOST_CLUSTER_ID)).thenReturn(cluster);
        doNothing().when(auditLogDao).save(any(AuditLog.class));
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);
        when(dbFacade.getVdsGroupDao()).thenReturn(vdsGroupDao);
        when(dbFacade.getAuditLogDao()).thenReturn(auditLogDao);
        DbFacadeLocator.setDbFacade(dbFacade);
    }

    private VDSGroup mockCluster() {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(FENCECD_HOST_CLUSTER_ID);
        cluster.setCompatibilityVersion(CLUSTER_VERSION);
        cluster.setArchitecture(CLUSTER_ARCHITECTURE_TYPE);
        return cluster;
    }

    private void mockVds() {
        when(vds.getId()).thenReturn(FENCECD_HOST_ID);
        when(vds.getVdsGroupId()).thenReturn(FENCECD_HOST_CLUSTER_ID);
        when(vds.getStoragePoolId()).thenReturn(FENCED_HOST_DATACENTER_ID);
        when(vds.getStaticData()).thenReturn(vdsStatic);
        when(vds.getSpmStatus()).thenReturn(VdsSpmStatus.Contending);
        List<FenceAgent> agents = new LinkedList<>();
        agents.add(createAgent());
        when(vds.getFenceAgents()).thenReturn(agents);
    }

    private void mockProxyHost() {
        mockProxyHost(false);
    }

    private void mockProxyHost(boolean anotherProxyAvailable) {
        VDS proxyHost = new VDS();
        proxyHost.setId(PROXY_HOST_ID);
        when(proxyLocator.findProxyHost()).thenReturn(proxyHost);
        when(proxyLocator.findProxyHost(true)).thenReturn(proxyHost);
        VDS secondProxyHost = new VDS();
        if (anotherProxyAvailable) {
            secondProxyHost.setId(SECOND_PROXY_HOST_ID);
            when(proxyLocator.findProxyHost(true, PROXY_HOST_ID)).thenReturn(secondProxyHost);
        } else {
            when(proxyLocator.findProxyHost(true, PROXY_HOST_ID)).thenReturn(null);
        }
    }

    private void mockFenceSuccess() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.FenceVds), any(GetDeviceListVDSCommandParameters.class))).thenReturn(returnValue);
    }

    private void mockFenceFailure(boolean succeedOnSecondAttempt) {
        VDSReturnValue firstReturnValue = new VDSReturnValue();
        firstReturnValue.setSucceeded(false);
        VDSReturnValue secondReturnValue = new VDSReturnValue();
        secondReturnValue.setSucceeded(succeedOnSecondAttempt ? true : false);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.FenceVds), any(GetDeviceListVDSCommandParameters.class))).thenReturn(firstReturnValue)
                .thenReturn(secondReturnValue);
    }

    /**
     * Test that the correct error-message is conveyed when no proxy host is found.
     */
    @Test
    public void checkStatus_handleProxyNotFound() {
        when(proxyLocator.findProxyHost()).thenReturn(null);
        VDSFenceReturnValue result = executor.checkHostStatus();
        assertFalse(result.getSucceeded());
        assertTrue(result.getExceptionString().contains("no running proxy Host was found"));
    }

    /**
     * Test that the return value is correct when fencing succeeds. The return value should contain succeeded=true and
     * the agent used.
     */
    @Test
    public void successfulFence() {
        mockProxyHost();
        mockFenceSuccess();
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Start, agent);
        assertTrue(result.getSucceeded());
        assertEquals(result.getFenceAgentUsed(), agent);
    }

    /**
     * Test that SPM is stopped when Fence.Stop is activated.
     */
    @Test
    public void successfullSpmFence() {
        mockProxyHost();
        mockFenceSuccess();
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Stop, agent);
        verify(vdsBrokerFrontend).RunVdsCommand(eq(VDSCommandType.SpmStop), any(VDSParametersBase.class));
        assertTrue(result.getSucceeded());
        assertEquals(result.getFenceAgentUsed(), agent);
    }


    /**
     * Test that when first fence attempt fails, fence is retried with a different proxy.
     */
    @Test
    public void successfulFenceWithDifferentProxyRetry() {
        mockProxyHost(true);
        mockFenceFailure(true);
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Start, agent);
        assertTrue(result.getSucceeded());
        verify(proxyLocator).findProxyHost(true, PROXY_HOST_ID);
    }

    /**
     * Test that when first fence attempt fails, and no alternative proxy is found, fence is retried with the same
     * proxy.
     */
    @Test
    public void successfulFenceWithSameProxyRetry() {
        mockProxyHost(false);
        mockFenceFailure(true);
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Start, agent);
        assertTrue(result.getSucceeded());
        verify(proxyLocator).findProxyHost(true, PROXY_HOST_ID);
    }

    /**
     * Test that when first fence attempt fails, and also the second attempt, using a different proxy, fails, the return
     * value contains succceeded=false.
     */
    @Test
    public void failedFenceWithDifferentProxyRetry() {
        mockProxyHost(true);
        mockFenceFailure(false);
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Start, agent);
        assertFalse(result.getSucceeded());
        verify(proxyLocator).findProxyHost(true, PROXY_HOST_ID);
    }

    /**
     * Test that when first fence attempt fails, and also the second attempt, using the smae proxy, fails, the return
     * value contains succceeded=false.
     */
    @Test
    public void failedFenceWithSameProxyRetry() {
        mockProxyHost(false);
        mockFenceFailure(false);
        FenceAgent agent = createAgent();
        VDSFenceReturnValue result = executor.fence(FenceActionType.Start, agent);
        assertFalse(result.getSucceeded());
        verify(proxyLocator).findProxyHost(true, PROXY_HOST_ID);
    }


    /**
     * Tests that when at least one agent returns 'on', checkHostStatus() returns 'on'. Mocking makes the first agent
     * return 'off'. FenceExecutor then tries the next agent, which is mocked to return true.
     */
    @Test
    public void checkHostStatusOn() {
        mockProxyHost();
        VDSReturnValue returnValueOff = new VDSReturnValue();
        returnValueOff.setSucceeded(true);
        FenceStatusReturnValue statusOff = new FenceStatusReturnValue("off", "");
        returnValueOff.setReturnValue(statusOff);
        VDSReturnValue returnValueOn = new VDSReturnValue();
        returnValueOn.setSucceeded(true);
        FenceStatusReturnValue statusOn = new FenceStatusReturnValue("on", "");
        returnValueOn.setReturnValue(statusOn);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.FenceVds), any(GetDeviceListVDSCommandParameters.class))).thenReturn(returnValueOff)
                .thenReturn(returnValueOn);
        List<FenceAgent> agents = new LinkedList<>();
        agents.add(createAgent());
        agents.add(createAgent());
        when(vds.getFenceAgents()).thenReturn(agents);
        VDSFenceReturnValue result = executor.checkHostStatus();
        assertTrue(result.getSucceeded());
        assertTrue(result.getReturnValue() instanceof FenceStatusReturnValue);
        FenceStatusReturnValue status = (FenceStatusReturnValue) result.getReturnValue();
        assertEquals(status.getStatus(), "on");
    }

    /**
     * Tests that when no even a single agent returns 'on', checkHostStatus() returns 'off'. Two agents are mocked to
     * return status 'off'. FenceExecutor tries both of them and returns status=off
     */
    @Test
    public void checkHostStatusOff() {
        mockProxyHost();
        VDSReturnValue returnValueOff = new VDSReturnValue();
        returnValueOff.setSucceeded(true);
        FenceStatusReturnValue statusOff = new FenceStatusReturnValue("off", "");
        returnValueOff.setReturnValue(statusOff);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.FenceVds), any(GetDeviceListVDSCommandParameters.class))).thenReturn(returnValueOff)
                .thenReturn(returnValueOff);
        List<FenceAgent> agents = new LinkedList<>();
        agents.add(createAgent());
        agents.add(createAgent());
        when(vds.getFenceAgents()).thenReturn(agents);
        VDSFenceReturnValue result = executor.checkHostStatus();
        assertTrue(result.getSucceeded());
        assertTrue(result.getReturnValue() instanceof FenceStatusReturnValue);
        FenceStatusReturnValue status = (FenceStatusReturnValue) result.getReturnValue();
        assertEquals(status.getStatus(), "off");
    }


    private FenceAgent createAgent() {
        FenceAgent agent = new FenceAgent();
        agent.setId(FENCE_AGENT_ID);
        return agent;
    }

}
