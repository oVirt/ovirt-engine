package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private ClusterDao clusterDaoMock;

    @Mock
    private NetworkClusterDao networkClusterDaoMock;

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
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }

    private void setupNetworkDao() {
        when(networkDaoMock.get(networkId)).thenReturn(network);
    }

    private void setupNetworkClusterDao() {
        List<Cluster> expectedCluster = Collections.singletonList(cluster);
        when(clusterDaoMock.getAllForStoragePool(storagePoolId)).thenReturn(expectedCluster);
    }

    private void setupClusterDao() {
        List<NetworkCluster> expectedNetworkCluster = Collections.singletonList(networkCluster);
        when(networkClusterDaoMock.getAllForNetwork(networkId)).thenReturn(expectedNetworkCluster);
    }
}
