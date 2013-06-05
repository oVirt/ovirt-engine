package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.InterfaceDao;

/**
 * A test for the {@link GetVdsAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the DAO}). The internal workings of the DAO are not tested.
 */
public class GetVdsAndNetworkInterfacesByNetworkIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetVdsAndNetworkInterfacesByNetworkIdQuery<IdQueryParameters>> {

    private Guid networkId = Guid.newGuid();
    private VDS vds = new VDS();
    private VdsNetworkInterface vdsNetworkInterface = new VdsNetworkInterface();

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getId()).thenReturn(networkId);

        setupVdsDao();
        setupVdsNetworkInterfaceDao();

        PairQueryable<VdsNetworkInterface, VDS> vdsInterfaceVdsPair =
                new PairQueryable<VdsNetworkInterface, VDS>(vdsNetworkInterface, vds);
        List<PairQueryable<VdsNetworkInterface, VDS>> expected = Collections.singletonList(vdsInterfaceVdsPair);

        // Run the query
        GetVdsAndNetworkInterfacesByNetworkIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupVdsDao() {
        List<VDS> expectedVds = Collections.singletonList(vds);
        VdsDAO vdsDaoMock = mock(VdsDAO.class);
        when(vdsDaoMock.getAllForNetwork(networkId)).thenReturn(expectedVds);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDaoMock);
    }

    private void setupVdsNetworkInterfaceDao() {
        List<VdsNetworkInterface> expectedVdsNetworkInterface = Collections.singletonList(vdsNetworkInterface);
        InterfaceDao vdsNetworkInterfaceDaoMock = mock(InterfaceDao.class);
        when(vdsNetworkInterfaceDaoMock.getVdsInterfacesByNetworkId(networkId)).thenReturn(expectedVdsNetworkInterface);
        when(getDbFacadeMockInstance().getInterfaceDao()).thenReturn(vdsNetworkInterfaceDaoMock);
    }
}
