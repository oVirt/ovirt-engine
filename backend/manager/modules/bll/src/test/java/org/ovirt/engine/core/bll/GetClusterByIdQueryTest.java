package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

/**
 * A test case for {@link GetClusterByIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetClusterByIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetClusterByIdQuery<IdQueryParameters>> {

    @Mock
    private ClusterDao clusterDaoMock;

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid clusterID = Guid.newGuid();
        Cluster expected = new Cluster();
        expected.setId(clusterID);

        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(clusterID);

        // Mock the Daos
        when(clusterDaoMock.get(clusterID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(expected);

        getQuery().executeQueryCommand();

        Cluster actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(expected, actual, "wrong VDS Group");
    }
}
