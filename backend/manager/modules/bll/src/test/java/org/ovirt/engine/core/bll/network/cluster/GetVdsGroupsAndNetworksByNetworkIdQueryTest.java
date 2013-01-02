package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
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
    private NetworkCluster networkCluster = new NetworkCluster();

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getNetworkId()).thenReturn(networkId);

        network.setId(networkId);
        network.setDataCenterId(storagePoolId);
        cluster.setId(clusterId);
        networkCluster.setClusterId(clusterId);
        networkCluster.setNetworkId(networkId);

        setupNetworkDao();
        setupVdsGroupDao();
        setupNetworkClusterDao();

        PairQueryable<VDSGroup, NetworkCluster> networkClusterPair =
                new PairQueryable<VDSGroup, NetworkCluster>(cluster, networkCluster);
        List<PairQueryable<VDSGroup, NetworkCluster>> expected = Collections.singletonList(networkClusterPair);

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
        List<NetworkCluster> expectedNetworkCluster = Collections.singletonList(networkCluster);
        NetworkClusterDAO networkClusterDaoMock = mock(NetworkClusterDAO.class);
        when(networkClusterDaoMock.getAllForNetwork(networkId)).thenReturn(expectedNetworkCluster);
        when(getDbFacadeMockInstance().getNetworkClusterDao()).thenReturn(networkClusterDaoMock);
    }
}
