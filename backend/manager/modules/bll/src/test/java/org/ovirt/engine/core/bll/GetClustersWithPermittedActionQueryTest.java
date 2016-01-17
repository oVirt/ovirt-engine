package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.ClusterDao;

/**
 * A test case for {@link GetClustersWithPermittedActionQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetClustersWithPermittedActionQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetEntitiesWithPermittedActionParameters, GetClustersWithPermittedActionQuery<GetEntitiesWithPermittedActionParameters>> {

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        Cluster expected = new Cluster();

        // Mock the Dao
        ClusterDao clusterDaoMock = mock(ClusterDao.class);
        when(clusterDaoMock.getClustersWithPermittedAction(getUser().getId(), getActionGroup())).thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getClusterDao()).thenReturn(clusterDaoMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Cluster> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VDS Groups", 1, actual.size());
        assertEquals("Wrong VDS Groups", expected, actual.get(0));
    }
}
