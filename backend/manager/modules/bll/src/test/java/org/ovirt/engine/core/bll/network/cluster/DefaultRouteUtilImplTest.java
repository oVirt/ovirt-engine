package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRouteUtilImplTest {

    private Guid networkId = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();

    @Mock
    private NetworkClusterDao networkClusterDao;

    @InjectMocks
    private DefaultRouteUtilImpl underTest;

    @Test
    public void testIsDefaultRouteWhenNetworkClusterDoesNotExist() throws Exception {
        when(networkClusterDao.get(new NetworkClusterId(clusterId, networkId))).thenReturn(null);

        assertThat(underTest.isDefaultRouteNetwork(networkId, clusterId), is(false));
    }

    @Test
    public void testIsDefaultRouteWhenNetworkClusterExistsAndIsDefaultRoute() throws Exception {
        testIsDefaultRouteWhenNetworkClusterExist(true);
    }

    @Test
    public void testIsDefaultRouteWhenNetworkClusterExistsAndIsntDefaultRoute() throws Exception {
        testIsDefaultRouteWhenNetworkClusterExist(false);
    }

    private void testIsDefaultRouteWhenNetworkClusterExist(boolean isDefaultRoute) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDefaultRoute(isDefaultRoute);
        when(networkClusterDao.get(new NetworkClusterId(clusterId, networkId))).thenReturn(networkCluster);

        assertThat(underTest.isDefaultRouteNetwork(networkId, clusterId), is(isDefaultRoute));
    }



}
