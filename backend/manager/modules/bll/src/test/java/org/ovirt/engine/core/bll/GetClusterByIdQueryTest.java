package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
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

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid clusterID = Guid.newGuid();
        Cluster expected = new Cluster();
        expected.setId(clusterID);

        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(clusterID);

        // Mock the Daos
        ClusterDao vdsGropDaoMock = mock(ClusterDao.class);
        when(vdsGropDaoMock.get(clusterID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(expected);
        when(getDbFacadeMockInstance().getClusterDao()).thenReturn(vdsGropDaoMock);

        getQuery().executeQueryCommand();

        Cluster actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("wrong VDS Group", expected, actual);
    }
}
