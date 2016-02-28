package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class FenceValidatorTest {

    @Mock
    private FenceValidator validator;

    @Mock
    private FenceProxyLocator proxyLocator;

    @Mock
    private BackendInternal backend;

    @Mock
    private IConfigUtilsInterface configUtils;

    @Before
    public void setup() {
        validator = new FenceValidator();
        validator = spy(validator);
        stub(validator.getProxyLocator(any(VDS.class))).toReturn(proxyLocator);
        doReturn(backend).when(validator).getBackend();
        Config.setConfigUtils(configUtils);
    }

    @Test
    public void failWhenProxyHostNotAvailable() {
        when(proxyLocator.isProxyHostAvailable()).thenReturn(false);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isProxyHostAvailable(new VDS(), messages);
        assertFalse(result);
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "VDS_NO_VDS_PROXY_FOUND");
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
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "ACTION_TYPE_FAILED_HOST_NOT_EXIST");
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
        when(configUtils.getValue(eq(ConfigValues.DisableFenceAtStartupInSec), any(String.class))).thenReturn(5);
        when(backend.getStartedAt()).thenReturn(new DateTime(new Date()));
        boolean result = validator.isStartupTimeoutPassed(messages);
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL");
        assertFalse(result);
    }

    @Test
    public void succeedWhenStartupTimeoutHasPassed() {
        List<String> messages = new LinkedList<>();
        when(configUtils.getValue(eq(ConfigValues.DisableFenceAtStartupInSec), any(String.class))).thenReturn(5);
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
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "VDS_FENCE_DISABLED");
    }

    @Test
    public void failWhenNoAgentsExist() {
        VDS vds = new VDS();
        vds.setPmEnabled(true);
        List<String> messages = new LinkedList<>();
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, null, messages);
        assertFalse(result);
        assertEquals(messages.size(), 2);
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
        assertEquals(messages.size(), 2);
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
        when(configUtils.getValue(eq(ConfigValues.VdsFenceType), any(String.class))).thenReturn("apc");
        when(configUtils.getValue(eq(ConfigValues.CustomVdsFenceType), any(String.class))).thenReturn("apc");
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, cluster, messages);
        assertTrue(result);
    }

}
