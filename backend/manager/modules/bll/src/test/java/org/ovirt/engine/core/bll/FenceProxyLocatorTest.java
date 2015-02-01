package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * This class tests the FenceProxyLocator
 */
@RunWith(MockitoJUnitRunner.class)
public class FenceProxyLocatorTest extends DbDependentTestBase {

    private static String HOST_NAME = "hostname";
    private static String ANOTHER_HOST_NAME = "hostname2";
    private static Guid FENCECD_HOST_ID = new Guid("11111111-1111-1111-1111-111111111111");
    private static Guid FENCED_HOST_CLUSTER_ID = new Guid("22222222-2222-2222-2222-222222222222");
    private static Guid FENCED_HOST_DATACENTER_ID = new Guid("33333333-3333-3333-3333-333333333333");
    private static Guid OTHER_HOST_ID_1 = new Guid("55555555-5555-5555-5555-555555555555");
    private static Guid OTHER_HOST_ID_2 = Guid.Empty;
    private static Guid OTHER_CLUSTER_ID = new Guid("66666666-6666-6666-6666-666666666666");
    private static Guid OTHER_CLUSTER_ID_2 = new Guid("88888888-8888-8888-8888-888888888888");
    private static Guid OTHER_DATACENTER_ID = new Guid("77777777-7777-7777-7777-777777777777");

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.FindFenceProxyRetries, 2),
                    MockConfigRule.mockConfig(ConfigValues.FindFenceProxyDelayBetweenRetriesInSec, 2),
                    MockConfigRule.mockConfig(ConfigValues.FenceProxyDefaultPreferences, "cluster,dc,other_dc"),
                    MockConfigRule.mockConfig(ConfigValues.VdsFenceOptionTypes, "secure=bool,port=int,slot=int"));

    @Mock
    private VDS fencedVds;

    private DbFacade dbFacade;

    @Mock
    private VdsDAO vdsDao;

    private FenceProxyLocator fenceProxyLocator;

    @Before
    public void setup() {
        dbFacade = DbFacade.getInstance();
        when(fencedVds.getName()).thenReturn(HOST_NAME);
        when(fencedVds.getId()).thenReturn(FENCECD_HOST_ID);
        when(fencedVds.getVdsGroupId()).thenReturn(FENCED_HOST_CLUSTER_ID);
        when(fencedVds.getStoragePoolId()).thenReturn(FENCED_HOST_DATACENTER_ID);
        fenceProxyLocator = new FenceProxyLocator(fencedVds);
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);
    }

    /**
     * Tests flow where a proxy host is succesfully found
     */
    @Test
    public void findProxyHost() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = createProxyCandidate();
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost();
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_1);
    }

    /**
     * Tests that the locator doesn't choose the fenced-host as a proxy host. Validates that if this host is the only
     * host in the list of proxy candidates, the method returns null.
     */
    @Test
    public void findProxyHostExcludeSelf() {
        List<VDS> hosts = new LinkedList<>();
        hosts.add(fencedVds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost();
        assertNull(proxyHost);
    }


    /**
     * Tests that the locator doesn't choose the fenced-host as a proxy host. Validates that if there are additional
     * hosts in the list of proxy candidates, the fence-host is skipped and another host is chosen.
     */
    @Test
    public void findProxyHostExcludeSelfAnotherHostAvailable() {
        List<VDS> hosts = new LinkedList<>();
        hosts.add(fencedVds);
        hosts.add(createProxyCandidate());
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost();
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_1);
    }


    /**
     * Tests that when a certain host is provided as one which should not be selected as proxy, it is indeed skipped.
     * This test validates that if this host is the only host in the list of proxy candidates, the method returns null.
     */
    @Test
    public void findProxyHostExcludeHost() {
        List<VDS> hosts = new LinkedList<>();
        hosts.add(createProxyCandidate());
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false, OTHER_HOST_ID_1);
        assertNull(proxyHost);
    }


    /**
     * Tests that when a certain host is provided as one which should not be selected as proxy, it is indeed skipped.
     * This test validates that if there are other hosts in the list of proxy candidates, one of them will be chosen,
     * and not the host that should be skipped.
     */
    @Test
    public void findProxyHostExcludeHostAnotherHostAvailable() {
        List<VDS> hosts = new LinkedList<>();
        hosts.add(createProxyCandidate());
        VDS anotherHost = createProxyCandidate();
        anotherHost.setId(OTHER_HOST_ID_2);
        hosts.add(anotherHost);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false, OTHER_HOST_ID_1);
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_2);
    }


    /**
     * Tests that an unreachable host (a host in 'NonResponsive' state) isn't chosen as proxy. This test validates that
     * if such a host is the only host in the list of proxy candidates, the method returns null.
     */
    @Test
    public void findProxyHostExcludeUnreachable() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = new VDS();
        vds.setId(OTHER_HOST_ID_1);
        vds.setStatus(VDSStatus.NonResponsive);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        vds.getDynamicData().setNonOperationalReason(NonOperationalReason.NETWORK_UNREACHABLE);
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNull(proxyHost);
    }


    /**
     * Tests that an unreachable host (a host in 'NonResponsive' state) isn't chosen as proxy. This test validates that
     * if such there are additional hosts in the list of proxy candidates, one of them will be chosen, and not the
     * unreachable host.
     */
    @Test
    public void findProxyHostExcludeUnreachableAnotherHostAvailable() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = new VDS();
        vds.setId(OTHER_HOST_ID_2);
        vds.setStatus(VDSStatus.NonResponsive);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        vds.getDynamicData().setNonOperationalReason(NonOperationalReason.NETWORK_UNREACHABLE);
        hosts.add(vds);
        hosts.add(createProxyCandidate());
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_1);
    }


    /**
     * Tests that the proxy locator will prefer a host from the same cluster as the fenced-host over a host from a
     * different cluster (the test assumes: FenceProxyDefaultPreferences="cluster,dc,other_dc")
     */
    @Test
    public void preferProxyHostFromSameCluster() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = new VDS();
        vds.setId(OTHER_HOST_ID_1);
        vds.setVdsGroupId(OTHER_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        vds = new VDS();
        vds.setId(OTHER_HOST_ID_2);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_2);
    }

    /**
     * Tests that, in a situation where there is no host availabe from the same cluster, the locator will prefer a host
     * from the same datacenter as the fenced-host over a host from a different datacenter (the test assumes:
     * FenceProxyDefaultPreferences="cluster,dc,other_dc")
     */
    @Test
    public void preferProxyHostFromSameDatacenter() {
        when(vdsDao.getAll()).thenReturn(createHosts()).thenReturn(createHosts());
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_2);
    }


    /**
     * Tests that the locator will prefer an 'UP' host as proxy over a host in 'Maintenance' mode.
     */
    @Test
    public void findProxyHostPreferUpHost() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = new VDS();
        vds.setId(OTHER_HOST_ID_1);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        vds.setStatus(VDSStatus.Maintenance);
        hosts.add(vds);
        vds = new VDS();
        vds.setId(OTHER_HOST_ID_2);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        vds.setStatus(VDSStatus.Up);
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNotNull(proxyHost);
        assertEquals(proxyHost.getId(), OTHER_HOST_ID_2);
    }


    /**
     * Tests version compatibility when a fencing-policy is defined. In this test, the validation is successful because
     * the minimal version supporting fencing-policy exists in the hosts 'supportedClusterLevels' list.
     */
    @Test
    public void findProxyHostFencingPolicySupported() {
        FencingPolicy policy = new FencingPolicy();
        fenceProxyLocator.setFencingPolicy(policy);
        VDS vds = new VDS();
        vds.setSupportedClusterLevels(Version.v3_0.toString());
        vds.setId(OTHER_HOST_ID_1);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        List<VDS> hosts = new LinkedList<>();
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNotNull(proxyHost);
    }


    /**
     * Tests version compatibility when a fencing-policy is defined. In this test, the validation fails because the
     * minimal version supporting fencing-policy does not exist in the hosts 'supportedClusterLevels' list.
     */
    @Test
    public void findProxyHostFencingPolicyNotSupported() {
        FencingPolicy policy = new FencingPolicy();
        fenceProxyLocator.setFencingPolicy(policy);
        VDS vds = new VDS();
        vds.setSupportedClusterLevels(Version.v3_1.toString());
        vds.setId(OTHER_HOST_ID_1);
        vds.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        List<VDS> hosts = new LinkedList<>();
        hosts.add(vds);
        when(vdsDao.getAll()).thenReturn(hosts);
        VDS proxyHost = fenceProxyLocator.findProxyHost(false);
        assertNull(proxyHost);
    }


    private List<VDS> createHosts() {
        List<VDS> hosts = new LinkedList<>();
        VDS vds = new VDS();
        vds.setId(OTHER_HOST_ID_1);
        vds.setVdsGroupId(OTHER_CLUSTER_ID);
        vds.setStoragePoolId(OTHER_DATACENTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        hosts.add(vds);
        vds = new VDS();
        vds.setId(OTHER_HOST_ID_2);
        vds.setVdsGroupId(OTHER_CLUSTER_ID_2);
        vds.setStoragePoolId(FENCED_HOST_DATACENTER_ID);
        vds.setVdsGroupCompatibilityVersion(Version.v3_5);
        hosts.add(vds);
        return hosts;
    }

    private VDS createProxyCandidate() {
        VDS proxyCandidate = new VDS();
        proxyCandidate.setVdsName(ANOTHER_HOST_NAME);
        proxyCandidate.setId(OTHER_HOST_ID_1);
        proxyCandidate.setVdsGroupId(FENCED_HOST_CLUSTER_ID);
        proxyCandidate.setStoragePoolId(FENCED_HOST_DATACENTER_ID);
        proxyCandidate.setVdsGroupCompatibilityVersion(Version.v3_0);
        return proxyCandidate;
    }
}
