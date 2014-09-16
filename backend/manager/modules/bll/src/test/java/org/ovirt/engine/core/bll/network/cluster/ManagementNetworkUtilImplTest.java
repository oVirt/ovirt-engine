package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@RunWith(MockitoJUnitRunner.class)
public class ManagementNetworkUtilImplTest {

    private static final Guid TEST_CLUSTER_ID = Guid.newGuid();
    private static final Guid TEST_NETWORK_ID = Guid.newGuid();
    private static final NetworkClusterId TEST_NETWORK_CLUSTER_ID =
            new NetworkClusterId(TEST_CLUSTER_ID, TEST_NETWORK_ID);

    private ManagementNetworkUtil underTest;

    @Mock
    private DbFacade mockDbFacade;
    @Mock
    private NetworkDao mockNetworkDao;
    @Mock
    private NetworkClusterDao mockNetworkClusterDao;

    @Mock
    private Network mockNetwork;
    @Mock
    private NetworkCluster mockNetworkCluster;

    @Before
    public void setUp() throws Exception {
        when(mockDbFacade.getNetworkDao()).thenReturn(mockNetworkDao);
        when(mockDbFacade.getNetworkClusterDao()).thenReturn(mockNetworkClusterDao);

        DbFacadeLocator.setDbFacade(mockDbFacade);

        underTest = new ManagementNetworkUtilImpl();
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
        when(mockNetworkClusterDao.getAllForNetwork(TEST_NETWORK_ID))
                .thenReturn(Collections.singletonList(mockNetworkCluster));

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
        when(mockNetworkClusterDao.get(eq(TEST_NETWORK_CLUSTER_ID))).thenReturn(mockNetworkCluster);
        when(mockNetworkCluster.isManagement()).thenReturn(expectedResult);

        final boolean actual = underTest.isManagementNetwork(TEST_NETWORK_ID, TEST_CLUSTER_ID);

        assertEquals(expectedResult, actual);
    }
}
