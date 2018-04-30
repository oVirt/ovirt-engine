package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllNetworksByClusterIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllNetworksByClusterIdQuery<? extends IdQueryParameters>> {

    @Mock
    private NetworkDao networkDaoMock;

    /** Tests that {@link GetAllNetworksByClusterIdQuery#executeQueryCommand()} delegated to the correct Daos, using mock objects */
    @Test
    public void testExecuteQueryCommand() {
        Guid clusterID = Guid.newGuid();

        Network networkMock = mock(Network.class);

        when(networkDaoMock.getAllForCluster(clusterID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(Collections.singletonList(networkMock));

        when(getQueryParameters().getId()).thenReturn(clusterID);
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Network> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, result.size(), "Wrong number of networks in result");
        assertEquals(networkMock, result.get(0), "Wrong network in result");
    }
}
