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
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * A test for the {@link GetVdsGroupsAndNetworksByNetworkIdQuery} class. It tests the flow (i.e., that the query delegates properly
 * to the Dao}). The internal workings of the Dao are not tested.
 */
public class GetVdsGroupsAndNetworksByNetworkIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetVdsGroupsAndNetworksByNetworkIdQuery<IdQueryParameters>> {

    private Guid networkId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();
    private Network network = new Network();
    private VDSGroup cluster = new VDSGroup();
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
        setupVdsGroupDao();
        setupNetworkClusterDao();

        PairQueryable<VDSGroup, NetworkCluster> networkClusterPair =
                new PairQueryable<>(cluster, networkCluster);
        List<PairQueryable<VDSGroup, NetworkCluster>> expected = Collections.singletonList(networkClusterPair);

        // Run the query
        GetVdsGroupsAndNetworksByNetworkIdQuery<IdQueryParameters> query = getQuery();
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
        List<VDSGroup> expectedVdsGroup = Collections.singletonList(cluster);
        VdsGroupDao vdsGroupDaoMock = mock(VdsGroupDao.class);
        when(vdsGroupDaoMock.getAllForStoragePool(storagePoolId)).thenReturn(expectedVdsGroup);
        when(getDbFacadeMockInstance().getVdsGroupDao()).thenReturn(vdsGroupDaoMock);
    }

    private void setupVdsGroupDao() {
        List<NetworkCluster> expectedNetworkCluster = Collections.singletonList(networkCluster);
        NetworkClusterDao networkClusterDaoMock = mock(NetworkClusterDao.class);
        when(networkClusterDaoMock.getAllForNetwork(networkId)).thenReturn(expectedNetworkCluster);
        when(getDbFacadeMockInstance().getNetworkClusterDao()).thenReturn(networkClusterDaoMock);
    }
}
