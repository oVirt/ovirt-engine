package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class FenceValidatorTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Spy
    private FenceValidator validator;

    @Mock
    private FenceProxyLocator proxyLocator;

    @Mock
    private BackendInternal backend;

    @Before
    public void setup() {
        doReturn(proxyLocator).when(validator).getProxyLocator(any(VDS.class));
        doReturn(backend).when(validator).getBackend();
    }

    @Test
    public void failWhenProxyHostNotAvailable() {
        when(proxyLocator.isProxyHostAvailable()).thenReturn(false);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isProxyHostAvailable(new VDS(), messages);
        assertFalse(result);
        assertEquals(1, messages.size());
        assertEquals("VDS_NO_VDS_PROXY_FOUND", messages.get(0));
    }

    @Test
    public void succeedWhenProxyHostAvailable() {
        when(proxyLocator.isProxyHostAvailable()).thenReturn(true);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isProxyHostAvailable(new VDS(), messages);
        assertTrue(result);
        assertTrue(messages.isEmpty());
    }

    @Test
    public void failWhenHostDoesNotExist() {
        List<String> messages = new LinkedList<>();
        boolean result = validator.isHostExists(null, messages);
        assertFalse(result);
        assertEquals(1, messages.size());
        assertEquals("ACTION_TYPE_FAILED_HOST_NOT_EXIST", messages.get(0));
    }

    @Test
    public void succeedWhenHostExists() {
        List<String> messages = new LinkedList<>();
        boolean result = validator.isHostExists(new VDS(), messages);
        assertTrue(result);
        assertTrue(messages.isEmpty());
    }

    @Test
    public void failWhenStartupTimeoutHasNotPassed() {
        List<String> messages = new LinkedList<>();
        mcr.mockConfigValue(ConfigValues.DisableFenceAtStartupInSec, 5);
        when(backend.getStartedAt()).thenReturn(new DateTime(new Date()));
        boolean result = validator.isStartupTimeoutPassed(messages);
        assertEquals(1, messages.size());
        assertEquals("VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL", messages.get(0));
        assertFalse(result);
    }

    @Test
    public void succeedWhenStartupTimeoutHasPassed() {
        List<String> messages = new LinkedList<>();
        mcr.mockConfigValue(ConfigValues.DisableFenceAtStartupInSec, 5);
        when(backend.getStartedAt()).thenReturn(new DateTime(new Date().getTime() - 20000));
        boolean result = validator.isStartupTimeoutPassed(messages);
        assertTrue(result);
    }

    @Test
    public void failWhenPowerManagementDisabled() {
        VDS vds = new VDS();
        List<String> messages = new LinkedList<>();
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, null, messages);
        assertFalse(result);
        assertEquals(1, messages.size());
        assertEquals("VDS_FENCE_DISABLED", messages.get(0));
    }

    @Test
    public void failWhenNoAgentsExist() {
        VDS vds = new VDS();
        vds.setPmEnabled(true);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, null, messages);
        assertFalse(result);
        assertEquals(2, messages.size());
        assertTrue(messages.contains("ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT"));
        assertTrue(messages.contains("VDS_FENCE_DISABLED"));
    }

    @Test
    public void failWhenClusterVersionNotCompatible() {
        VDS vds = new VDS();
        vds.setPmEnabled(true);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.getLast());
        FenceAgent agent = new FenceAgent();
        agent.setType("Some_Type");
        vds.getFenceAgents().add(agent);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, cluster, messages);
        assertFalse(result);
        assertEquals(2, messages.size());
        assertTrue(messages.contains("ACTION_TYPE_FAILED_AGENT_NOT_SUPPORTED"));
        assertTrue(messages.contains("VDS_FENCE_DISABLED"));
    }

    @Test
    public void succeedWhenClusterVersionCompatible() {
        VDS vds = new VDS();
        vds.setPmEnabled(true);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.getLast());
        FenceAgent agent = new FenceAgent();
        agent.setType("apc");
        vds.getFenceAgents().add(agent);
        List<String> messages = new LinkedList<>();
        mcr.mockConfigValue(ConfigValues.VdsFenceType, Version.getLast(), "apc");
        mcr.mockConfigValue(ConfigValues.CustomVdsFenceType, Version.getLast(), "apc");
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, cluster, messages);
        assertTrue(result);
    }

}
