package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * A test for the {@link GetVdsWithoutNetworkQuery} class. It tests the flow (i.e., that the query delegates properly to
 * the Dao}). The internal workings of the Dao are not tested.
 */
public class GetVdsWithoutNetworkQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetVdsWithoutNetworkQuery<IdQueryParameters>> {

    @Mock
    private VdsDao vdsDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid networkId = Guid.newGuid();
        when(params.getId()).thenReturn(networkId);

        // Set up the Daos
        VDS vds = new VDS();
        List<VDS> expected = Collections.singletonList(vds);
        when(vdsDaoMock.getAllWithoutNetwork(networkId)).thenReturn(expected);

        // Run the query
        GetVdsWithoutNetworkQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }
}
