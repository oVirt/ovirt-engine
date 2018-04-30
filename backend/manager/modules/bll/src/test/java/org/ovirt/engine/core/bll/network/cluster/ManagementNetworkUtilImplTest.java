package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.DefaultManagementNetwork, TEST_NETWORK_NAME));
    }

    @BeforeEach
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
    public void testIsManagementNetworkInAClusterPositive() {
        testIsManagementNetworkInAClusterCommon(true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid)} .
     */
    @Test
    public void testIsManagementNetworkInAClusterNegative() {
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
    public void testIsManagementNetworkInGivenClusterPositive() {
        testIsManagementNetworkInGivenClusterCommon(true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(Guid, Guid)} .
     */
    @Test
    public void testIsManagementNetworkInGivenClusterNegative() {
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
    public void testIsManagementNetworkNameInGivenClusterPositive() {
        testIsManagementNetworkNameInGivenClusterCommon(TEST_NETWORK_NAME, TEST_CLUSTER_ID, true);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameInGivenClusterNegative() {
        testIsManagementNetworkNameInGivenClusterCommon("not" + TEST_NETWORK_NAME, TEST_CLUSTER_ID, false);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameInGivenClusterNull() {
        testIsManagementNetworkNameInGivenClusterCommon(TEST_NETWORK_NAME, null, false);
    }

    /**
     * Test method for {@link ManagementNetworkUtilImpl#isManagementNetwork(String, Guid)} .
     */
    @Test
    public void testIsManagementNetworkNameNullInGivenCluster() {
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
    public void testGetDefaultManagementNetworkName() {
        assertEquals(TEST_NETWORK_NAME, underTest.getDefaultManagementNetworkName());
    }

    @Test
    public void testGetManagementNetworkWhenInstancesDoesNotContainOne() {
        List<Network> networks = Arrays.asList(createNetwork(false, "a"), createNetwork(false, "b"));
        Guid clusterId = Guid.newGuid();

        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> underTest.getManagementNetwork(networks, clusterId));
        assertEquals(underTest.createFailureMessage(clusterId, networks), e.getMessage());
    }

    @Test
    public void testGetManagementNetworkWhenInstancesContainMultipleOnes() {
        List<Network> networks = Arrays.asList(createNetwork(true, "a"), createNetwork(true, "b"));

        Guid clusterId = Guid.newGuid();
        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> underTest.getManagementNetwork(networks, clusterId));
        assertEquals(underTest.createFailureMessage(clusterId, networks), e.getMessage());
    }

    @Test
    public void testGetManagementNetworkWhenInstancesContainOne() {
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
