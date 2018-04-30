package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@ExtendWith(MockitoExtension.class)
public class DefaultRouteUtilImplTest {

    private Guid networkId = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();

    @Mock
    private NetworkClusterDao networkClusterDao;

    @InjectMocks
    private DefaultRouteUtilImpl underTest;

    @Test
    public void testIsDefaultRouteWhenNetworkClusterDoesNotExist() {
        when(networkClusterDao.get(new NetworkClusterId(clusterId, networkId))).thenReturn(null);

        assertThat(underTest.isDefaultRouteNetwork(networkId, clusterId), is(false));
    }

    @Test
    public void testIsDefaultRouteWhenNetworkClusterExistsAndIsDefaultRoute() {
        testIsDefaultRouteWhenNetworkClusterExist(true);
    }

    @Test
    public void testIsDefaultRouteWhenNetworkClusterExistsAndIsntDefaultRoute() {
        testIsDefaultRouteWhenNetworkClusterExist(false);
    }

    private void testIsDefaultRouteWhenNetworkClusterExist(boolean isDefaultRoute) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDefaultRoute(isDefaultRoute);
        when(networkClusterDao.get(new NetworkClusterId(clusterId, networkId))).thenReturn(networkCluster);

        assertThat(underTest.isDefaultRouteNetwork(networkId, clusterId), is(isDefaultRoute));
    }



}
