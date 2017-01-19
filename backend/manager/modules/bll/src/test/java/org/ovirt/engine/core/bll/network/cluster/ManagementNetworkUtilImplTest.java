package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class ManagementNetworkUtilImplTest {

    private static final String TEST_NETWORK_NAME = "test network name";
    private static final Guid TEST_CLUSTER_ID = Guid.newGuid();
    private static final Guid TEST_NETWORK_ID = Guid.newGuid();
    private static final NetworkClusterId TEST_NETWORK_CLUSTER_ID =
            new NetworkClusterId(TEST_CLUSTER_ID, TEST_NETWORK_ID);

    private ManagementNetworkUtilImpl underTest;

    @Mock
    private NetworkDao mockNetworkDao;
    @Mock
    private NetworkClusterDao mockNetworkClusterDao;

    @Mock
    private Network mockNetwork;
    @Mock
    private NetworkCluster mockNetworkCluster;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.DefaultManagementNetwork, TEST_NETWORK_NAME));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        underTest = new ManagementNetworkUtilImpl(mockNetworkDao, mockNetworkClusterDao);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#getManagementNetwork(Guid)} .
     */
    @Test
    public void testGetManagementNetwork() {
        when(mockNetworkDao.getManagementNetwork(TEST_CLUSTER_ID)).thenReturn(mockNetwork);

        final Network actual = underTest.getManagementNetwork(TEST_CLUSTER_ID);

        assertSame(mockNetwork, actual);

        verify(mockNetworkDao).getManagementNetwork(TEST_CLUSTER_ID);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid)} .
     */
    @Test
    public void testIsManagementNetworkInAClusterPositive() throws Exception {
        testIsManagementNetworkInAClusterCommon(true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid)} .
     */
    @Test
    public void testIsManagementNetworkInAClusterNegative() throws Exception {
        testIsManagementNetworkInAClusterCommon(false);
    }

    private void testIsManagementNetworkInAClusterCommon(boolean expectedResult) {
        when(mockNetworkCluster.isManagement()).thenReturn(expectedResult);
        when(mockNetworkClusterDao.getAllForNetwork(TEST_NETWORK_ID)).thenReturn(Collections.singletonList(mockNetworkCluster));

        final boolean actual = underTest.isManagementNetwork(TEST_NETWORK_ID);

        assertEquals(expectedResult, actual);

        verify(mockNetworkClusterDao).getAllForNetwork(TEST_NETWORK_ID);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid, Guid)} .
     */
    @Test
    public void testIsManagementNetworkInGivenClusterPositive() throws Exception {
        testIsManagementNetworkInGivenClusterCommon(true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid, Guid)} .
     */
    @Test
    public void testIsManagementNetworkInGivenClusterNegative() throws Exception {
        testIsManagementNetworkInGivenClusterCommon(false);
    }

    private void testIsManagementNetworkInGivenClusterCommon(boolean expectedResult) {
        when(mockNetworkClusterDao.get(TEST_NETWORK_CLUSTER_ID)).thenReturn(mockNetworkCluster);
        when(mockNetworkCluster.isManagement()).thenReturn(expectedResult);

        final boolean actual = underTest.isManagementNetwork(TEST_NETWORK_ID, TEST_CLUSTER_ID);

        assertEquals(expectedResult, actual);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameInGivenClusterPositive() throws Exception {
        testIsManagementNetworkNameInGivenClusterCommon(TEST_NETWORK_NAME, TEST_CLUSTER_ID, true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameInGivenClusterNegative() throws Exception {
        testIsManagementNetworkNameInGivenClusterCommon("not" + TEST_NETWORK_NAME, TEST_CLUSTER_ID, false);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameInGivenClusterNull() throws Exception {
        testIsManagementNetworkNameInGivenClusterCommon(TEST_NETWORK_NAME, null, false);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameNullInGivenCluster() throws Exception {
        testIsManagementNetworkNameInGivenClusterCommon(null, TEST_CLUSTER_ID, false);
    }

    private void testIsManagementNetworkNameInGivenClusterCommon(String networkName,
                                                                 Guid clusterId,
                                                                 boolean expected) {
        when(mockNetworkDao.getManagementNetwork(TEST_CLUSTER_ID)).thenReturn(mockNetwork);
        when(mockNetwork.getName()).thenReturn(TEST_NETWORK_NAME);

        final boolean actual = underTest.isManagementNetwork(networkName, clusterId);

        verify(mockNetworkDao).getManagementNetwork(clusterId);

        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#getDefaultManagementNetworkName()}.
     */
    @Test
    public void testGetDefaultManagementNetworkName() throws Exception {
        assertEquals(TEST_NETWORK_NAME, underTest.getDefaultManagementNetworkName());
    }

    @Test
    public void testGetManagementNetworkWhenInstancesDoesNotContainOne() throws Exception {
        List<Network> networks = Arrays.asList(createNetwork(false, "a"), createNetwork(false, "b"));
        Guid clusterId = Guid.newGuid();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(underTest.createFailureMessage(clusterId, networks));
        underTest.getManagementNetwork(networks, clusterId);
    }

    @Test
    public void testGetManagementNetworkWhenInstancesContainMultipleOnes() throws Exception {
        List<Network> networks = Arrays.asList(createNetwork(true, "a"), createNetwork(true, "b"));

        Guid clusterId = Guid.newGuid();
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(underTest.createFailureMessage(clusterId, networks));
        underTest.getManagementNetwork(networks, clusterId);
    }

    @Test
    public void testGetManagementNetworkWhenInstancesContainOne() throws Exception {
        Network network = createNetwork(true, "a");
        List<Network> networks = Arrays.asList(network, createNetwork(false, "b"));
        assertThat(underTest.getManagementNetwork(networks, Guid.newGuid()), is(network));
    }

    private Network createNetwork(boolean isManagementNetwork, String name) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setManagement(isManagementNetwork);
        Network network = new Network();
        network.setName(name);
        network.setCluster(networkCluster);
        return network;
    }
}
