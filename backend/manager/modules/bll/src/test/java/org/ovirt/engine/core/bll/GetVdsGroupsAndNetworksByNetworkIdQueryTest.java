package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.NetworkClusterDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * A test for the {@link GetVdsGroupsAndNetworksByNetworkIdQuery} class. It tests the flow (i.e., that the query delegates properly
 * to the DAO}). The internal workings of the DAO are not tested.
 */
public class GetVdsGroupsAndNetworksByNetworkIdQueryTest
        extends AbstractQueryTest<NetworkIdParameters,
        GetVdsGroupsAndNetworksByNetworkIdQuery<NetworkIdParameters>> {

    private Guid networkId = Guid.NewGuid();
    private Guid storagePoolId = Guid.NewGuid();
    private Guid clusterId = Guid.NewGuid();
    private Network network = new Network();
    private VDSGroup cluster = new VDSGroup();
    private network_cluster networkCluster = new network_cluster();

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getNetworkId()).thenReturn(networkId);

        network.setId(networkId);
        network.setstorage_pool_id(storagePoolId);
        cluster.setId(clusterId);
        networkCluster.setcluster_id(clusterId);
        networkCluster.setnetwork_id(networkId);

        setupNetworkDao();
        setupVdsGroupDao();
        setupNetworkClusterDao();

        PairQueryable<VDSGroup, network_cluster> networkClusterPair =
                new PairQueryable<VDSGroup, network_cluster>(cluster, networkCluster);
        List<PairQueryable<VDSGroup, network_cluster>> expected = Collections.singletonList(networkClusterPair);

        // Run the query
        GetVdsGroupsAndNetworksByNetworkIdQuery<NetworkIdParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupNetworkDao() {
        NetworkDAO networkDaoMock = mock(NetworkDAO.class);
        when(networkDaoMock.get(networkId)).thenReturn(network);
        when(getDbFacadeMockInstance().getNetworkDao()).thenReturn(networkDaoMock);
    }

    private void setupNetworkClusterDao() {
        List<VDSGroup> expectedVdsGroup = Collections.singletonList(cluster);
        VdsGroupDAO vdsGroupDaoMock = mock(VdsGroupDAO.class);
        when(vdsGroupDaoMock.getAllForStoragePool(storagePoolId)).thenReturn(expectedVdsGroup);
        when(getDbFacadeMockInstance().getVdsGroupDao()).thenReturn(vdsGroupDaoMock);
    }

    private void setupVdsGroupDao() {
        List<network_cluster> expectedNetworkCluster = Collections.singletonList(networkCluster);
        NetworkClusterDAO networkClusterDaoMock = mock(NetworkClusterDAO.class);
        when(networkClusterDaoMock.getAllForNetwork(networkId)).thenReturn(expectedNetworkCluster);
        when(getDbFacadeMockInstance().getNetworkClusterDao()).thenReturn(networkClusterDaoMock);
    }
}
