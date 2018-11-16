package org.ovirt.engine.core.bll.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

/**
 * This class tests the FenceProxyLocator
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class FenceProxyLocatorTest extends BaseCommandTest {

    private static Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static Guid FENCED_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");
    private static Guid FENCED_HOST_DATACENTER_ID = new Guid("33333333-3333-3333-3333-333333333333");
    private static Guid OTHER_CLUSTER_ID = new Guid("66666666-6666-6666-6666-666666666666");
    private static Guid OTHER_CLUSTER_ID_2 = new Guid("88888888-8888-8888-8888-888888888888");
    private static Guid OTHER_DATACENTER_ID = new Guid("77777777-7777-7777-7777-777777777777");

    @Mock
    @InjectedMock
    public VdsDao vdsDao;

    @Mock
    @InjectedMock
    public FenceAgentDao fenceAgentDao;

    private VdsFenceOptions vdsFenceOptions;

    private VDS fencedHost;

    @BeforeEach
    public void setup() {
        mockVdsFenceOptions(true);
        mockFencedHost();
        mockFenceAgents();
    }

    @AfterEach
    public void cleanup() {
        fencedHost = null;
    }

    /**
     * Checks if locator select the only available host as a proxy.
     */
    @Test
    public void findProxyHost() {
        mockExistingHosts(createHost());

        VDS proxyHost = setupLocator().findProxyHost();

        assertNotNull(proxyHost);
    }

    /**
     * Checks if the locator excludes fenced host as a proxy host. And because fenced host is the only existing host,
     * no proxy is selected
     */
    @Test
    public void findProxyHostExcludesFencedHost() {
        mockExistingHosts(fencedHost);

        VDS proxyHost = setupLocator().findProxyHost();

        assertNull(proxyHost);
    }

    /**
     * Checks if the locator excludes fenced host as a proxy host and selects different available host.
     */
    @Test
    public void findProxyHostExcludeFencedHostWhenOtherHostAvailable() {
        mockExistingHosts(fencedHost, createHost());

        VDS proxyHost = setupLocator().findProxyHost();

        assertNotNull(proxyHost);
        assertNotEquals(proxyHost.getId(), fencedHost.getId());
    }

    /**
     * Checks if the locator excludes specified host as a proxy host. And because specified host is the only existing
     * host, no proxy is selected
     */
    @Test
    public void findProxyHostExcludesSpecifiedHost() {
        VDS excludedHost = createHost();
        mockExistingHosts(excludedHost);

        VDS proxyHost = setupLocator().findProxyHost(false, excludedHost.getId());

        assertNull(proxyHost);
    }

    /**
     * Checks if the locator excludes specified host as a proxy host and select different available host.
     */
    @Test
    public void findProxyHostExcludeHostAnotherHostAvailable() {
        VDS excludedHost = createHost();
        mockExistingHosts(excludedHost, createHost());

        VDS proxyHost = setupLocator().findProxyHost(false, excludedHost.getId());

        assertNotNull(proxyHost);
        assertNotEquals(proxyHost.getId(), excludedHost.getId());
    }

    /**
     * Checks if the locator excludes specified host as a proxy host, because it doesn't contain fence agents compatible
     * with agents defined for fenced host. And because specified host is the only existing host, no proxy is selected
     */
    @Test
    public void findProxyHostExcludesHostWithIncompatibleAgents() {
        VDS host = createHost();
        mockExistingHosts(host);
        mockVdsFenceOptions(false);

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNull(proxyHost);
    }

    /**
     * Checks if the locator excludes specified host as a proxy host, because its supported cluster level is lower
     * than minimal supported cluster level required by fencing policy. And because specified host is the only
     * existing host, no proxy is selected
     */
    @Test
    public void findProxyHostExcludesHostDueToFencingPolicy() {
        mockExistingHosts(createHost());
        FenceProxyLocator locator = setupLocator(new FencingPolicy());
        setMinSupportedVersionForFencingPolicy(locator, Version.v4_2);

        VDS proxyHost = locator.findProxyHost(false);

        assertNull(proxyHost);
    }

    /**
     * Checks if the locator excludes specified host as a proxy host, because it's unreachable by network. And because
     * specified host is the only existing host, no proxy is selected.
     *
     * Special case when host has NonOperational status and reason NETWORK_UNREACHABLE is tested in different test case!
     */
    @Test
    public void findProxyHostExcludesUnreachableHosts() {
        FenceProxyLocator locator = setupLocator();

        for (VDSStatus status : VDSStatus.values()) {
            mockExistingHosts(createHost(status));

            assertEquals(shouldHostBeUnreachable(status), locator.findProxyHost(false) == null);
        }
    }

    /**
     * Checks if the locator excludes NonOperational host as a proxy host, if it's NonOperationalReason is
     * NETWORK_UNREACHABLE. And because specified host is the only existing host, no proxy is selected.
     */
    @Test
    public void findProxyHostExcludesNonOperationalHosts() {
        mockExistingHosts(createHost());
        FenceProxyLocator locator = setupLocator();

        for (NonOperationalReason reason : NonOperationalReason.values()) {
            VDS host = createHost();
            host.setStatus(VDSStatus.NonOperational);
            host.setNonOperationalReason(reason);
            mockExistingHosts(host);

            assertEquals(reason == NonOperationalReason.NETWORK_UNREACHABLE, locator.findProxyHost(false) == null);
        }
    }

    /**
     * Checks if the locator will prefer an host in Up status as proxy over a host in NonOperational status.
     */
    @Test
    public void findProxyHostPreferUpHost() {
        VDS nonoperationalHost = createHost();
        nonoperationalHost.setStatus(VDSStatus.NonOperational);
        nonoperationalHost.setNonOperationalReason(NonOperationalReason.GENERAL);
        mockExistingHosts(nonoperationalHost, createHost());

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNotNull(proxyHost);
        assertNotEquals(proxyHost.getId(), nonoperationalHost.getId());
    }

    /**
     * Checks if the locator will select as proxy host in 'NonOperational'
     */
    @Test
    public void findProxyHostSelectSomeHostIfNoneUp() {
        VDS nonoperationalHost1 = createHost();
        nonoperationalHost1.setStatus(VDSStatus.NonOperational);
        nonoperationalHost1.setNonOperationalReason(NonOperationalReason.GENERAL);
        VDS nonoperationalHost2 = createHost();
        nonoperationalHost2.setStatus(VDSStatus.NonOperational);
        nonoperationalHost2.setNonOperationalReason(NonOperationalReason.GENERAL);
        mockExistingHosts(nonoperationalHost1, nonoperationalHost2);

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNotNull(proxyHost);
    }

    /**
     * Checks that the proxy locator will prefer a host from the same cluster as the fenced-host over a host from a
     * different cluster (assuming default fence proxy sources: "cluster,dc")
     */
    @Test
    public void preferProxyHostFromSameCluster() {
        mockExistingHosts(
                createHost(VDSStatus.Up, OTHER_CLUSTER_ID, FENCED_HOST_DATACENTER_ID),
                createHost(VDSStatus.Up, FENCED_HOST_CLUSTER_ID, FENCED_HOST_DATACENTER_ID));

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNotNull(proxyHost);
        assertEquals(proxyHost.getClusterId(), FENCED_HOST_CLUSTER_ID);
    }

    /**
     * Checks that, in a situation where there is no host available from the same cluster, the locator will select a host
     * from the same data center as the fenced host over a host from a different data center (assuming default fence
     * proxy sources: "cluster,dc")
     */
    @Test
    public void preferProxyHostFromSameDC() {
        mockExistingHosts(
                createHost(VDSStatus.Up, OTHER_CLUSTER_ID_2, OTHER_DATACENTER_ID),
                createHost(VDSStatus.Up, OTHER_CLUSTER_ID, FENCED_HOST_DATACENTER_ID));

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNotNull(proxyHost);
        assertEquals(proxyHost.getStoragePoolId(), FENCED_HOST_DATACENTER_ID);
    }

    /**
     * Checks that if fence proxy sources are set as "other_dc,cluster,dc", then available host from other DC is selected
     * as proxy although there's also an available host in the same cluster (BZ1131411)
     */
    @Test
    public void findProxyHostAccordingToOtherDcClusterDc() {
        mockProxySourcesForFencedHost(
                Arrays.asList(
                        FenceProxySourceType.OTHER_DC,
                        FenceProxySourceType.CLUSTER,
                        FenceProxySourceType.DC));
        mockExistingHosts(
                createHost(VDSStatus.Up, FENCED_HOST_CLUSTER_ID, FENCED_HOST_DATACENTER_ID),
                createHost(VDSStatus.Up, OTHER_CLUSTER_ID_2, OTHER_DATACENTER_ID));

        VDS proxyHost = setupLocator().findProxyHost(false);

        assertNotNull(proxyHost);
        assertEquals(proxyHost.getStoragePoolId(), OTHER_DATACENTER_ID);
    }

    /**
     * Checks comparison of host supported cluster level with minimal version requirement for fencing policy.
     */
    @Test
    public void testProxyCompatibilityWithFencingPolicy() {
        VDS host = createHost();
        host.setSupportedClusterLevels("4.2");
        FenceProxyLocator locator = setupLocator();

        assertTrue(locator.isFencingPolicySupported(host, Version.v4_2));
        assertFalse(locator.isFencingPolicySupported(host, Version.v4_3));
    }

    private void mockFencedHost() {
        fencedHost = mock(VDS.class);
        when(fencedHost.getId()).thenReturn(FENCECD_HOST_ID);
        when(fencedHost.getClusterId()).thenReturn(FENCED_HOST_CLUSTER_ID);
        when(fencedHost.getStoragePoolId()).thenReturn(FENCED_HOST_DATACENTER_ID);
        when(fencedHost.getHostName()).thenReturn("fencedHost");
    }

    private void mockProxySourcesForFencedHost(List<FenceProxySourceType> fenceProxySources) {
        when(fencedHost.getFenceProxySources()).thenReturn(fenceProxySources);
    }

    private FenceAgent createFenceAgent(Guid hostId, String type) {
        FenceAgent agent = new FenceAgent();
        agent.setId(Guid.newGuid());
        agent.setHostId(hostId);
        agent.setType(type);
        return agent;
    }

    private FenceProxyLocator setupLocator() {
        return setupLocator(null);
    }

    private FenceProxyLocator setupLocator(FencingPolicy fencingPolicy) {
        FenceProxyLocator fenceProxyLocator = spy(new FenceProxyLocator(fencedHost, fencingPolicy));
        doReturn(vdsFenceOptions).when(fenceProxyLocator).createVdsFenceOptions(any());
        doReturn(0L).when(fenceProxyLocator).getDelayBetweenRetries();
        doReturn(1).when(fenceProxyLocator).getFindFenceProxyRetries();
        doReturn(Arrays.asList(FenceProxySourceType.CLUSTER, FenceProxySourceType.DC))
                .when(fenceProxyLocator).getDefaultFenceProxySources();

        mockVdsFenceOptions(true);
        return fenceProxyLocator;
    }

    private void setMinSupportedVersionForFencingPolicy(FenceProxyLocator locator, Version version) {
        when(locator.getMinSupportedVersionForFencingPolicy()).thenReturn(version);
    }

    private VDS createHost() {
        return createHost(VDSStatus.Up);
    }

    private VDS createHost(VDSStatus status) {
        return createHost(status, FENCED_HOST_CLUSTER_ID, FENCED_HOST_DATACENTER_ID);
    }

    private VDS createHost(VDSStatus status, Guid clusterId, Guid dcId) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setClusterId(clusterId);
        host.setStoragePoolId(dcId);
        host.setClusterCompatibilityVersion(Version.getLast());
        host.setStatus(status);
        host.setHostName("host-" + host.getId());
        return host;
    }

    private void mockExistingHosts(final VDS... hosts) {
        // we need to recreate the list on each call, because the list is altered in FenceProxyLocator
        when(vdsDao.getAll()).thenAnswer(invocationOnMock -> Arrays.asList(hosts));
    }

    private void mockVdsFenceOptions(boolean agentsCompatibleWithProxy) {
        vdsFenceOptions = mock(VdsFenceOptions.class);
        when(vdsFenceOptions.isAgentSupported(any())).thenReturn(agentsCompatibleWithProxy);
    }

    private void mockFenceAgents() {
        when(fenceAgentDao.getFenceAgentsForHost(FENCECD_HOST_ID))
                .thenReturn(Collections.singletonList(createFenceAgent(FENCECD_HOST_ID, "ipmilan")));
    }

    private boolean shouldHostBeUnreachable(VDSStatus status) {
        return  status != VDSStatus.Up && status != VDSStatus.NonOperational;
    }
}
