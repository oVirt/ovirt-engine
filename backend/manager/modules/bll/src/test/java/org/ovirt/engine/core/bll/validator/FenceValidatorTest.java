package org.ovirt.engine.core.bll.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class FenceValidatorTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.DisableFenceAtStartupInSec, 5),
                MockConfigDescriptor.of(ConfigValues.VdsFenceType, Version.getLast(), "apc"),
                MockConfigDescriptor.of(ConfigValues.CustomVdsFenceType, "apc")
        );
    }

    @InjectMocks
    @Spy
    private FenceValidator validator;

    @Mock
    private FenceProxyLocator proxyLocator;

    @Mock
    private BackendInternal backend;

    @Mock
    private FenceAgentDao fenceAgentDao;

    @BeforeEach
    public void setup() {
        doReturn(proxyLocator).when(validator).getProxyLocator(any());
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
        when(backend.getStartedAt()).thenReturn(new DateTime(new Date()));
        boolean result = validator.isStartupTimeoutPassed(messages);
        assertEquals(1, messages.size());
        assertEquals("VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL", messages.get(0));
        assertFalse(result);
    }

    @Test
    public void succeedWhenStartupTimeoutHasPassed() {
        List<String> messages = new LinkedList<>();
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
        when(fenceAgentDao.getFenceAgentsForHost(vds.getId())).thenReturn(Collections.singletonList(agent));
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
        when(fenceAgentDao.getFenceAgentsForHost(vds.getId())).thenReturn(Collections.singletonList(agent));
        List<String> messages = new LinkedList<>();
        boolean result = validator.isPowerManagementEnabledAndLegal(vds, cluster, messages);
        assertTrue(result);
    }

}
