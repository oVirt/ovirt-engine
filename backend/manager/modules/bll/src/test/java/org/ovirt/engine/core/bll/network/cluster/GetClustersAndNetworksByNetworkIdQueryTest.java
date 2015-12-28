package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * A test for the {@link GetClustersAndNetworksByNetworkIdQuery} class. It tests the flow (i.e., that the query delegates properly
 * to the Dao}). The internal workings of the Dao are not tested.
 */
public class GetClustersAndNetworksByNetworkIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetClustersAndNetworksByNetworkIdQuery<IdQueryParameters>> {

    private Guid networkId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();
    private Network network = new Network();
    private Cluster cluster = new Cluster();
    private NetworkCluster networkCluster = new NetworkCluster();

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getId()).thenReturn(networkId);

        network.setId(networkId);
        network.setDataCenterId(storagePoolId);
        cluster.setId(clusterId);
        networkCluster.setClusterId(clusterId);
        networkCluster.setNetworkId(networkId);

        setupNetworkDao();
        setupClusterDao();
        setupNetworkClusterDao();

        PairQueryable<Cluster, NetworkCluster> networkClusterPair =
                new PairQueryable<>(cluster, networkCluster);
        List<PairQueryable<Cluster, NetworkCluster>> expected = Collections.singletonList(networkClusterPair);

        // Run the query
        GetClustersAndNetworksByNetworkIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupNetworkDao() {
        NetworkDao networkDaoMock = mock(NetworkDao.class);
        when(networkDaoMock.get(networkId)).thenReturn(network);
        when(getDbFacadeMockInstance().getNetworkDao()).thenReturn(networkDaoMock);
    }

    private void setupNetworkClusterDao() {
        List<Cluster> expectedCluster = Collections.singletonList(cluster);
        ClusterDao clusterDaoMock = mock(ClusterDao.class);
        when(clusterDaoMock.getAllForStoragePool(storagePoolId)).thenReturn(expectedCluster);
        when(getDbFacadeMockInstance().getClusterDao()).thenReturn(clusterDaoMock);
    }

    private void setupClusterDao() {
        List<NetworkCluster> expectedNetworkCluster = Collections.singletonList(networkCluster);
        NetworkClusterDao networkClusterDaoMock = mock(NetworkClusterDao.class);
        when(networkClusterDaoMock.getAllForNetwork(networkId)).thenReturn(expectedNetworkCluster);
        when(getDbFacadeMockInstance().getNetworkClusterDao()).thenReturn(networkClusterDaoMock);
    }
}
