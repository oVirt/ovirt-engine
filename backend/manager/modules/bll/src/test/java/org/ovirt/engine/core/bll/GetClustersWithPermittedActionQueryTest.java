package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock
    private ClusterDao clusterDaoMock;

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        Cluster expected = new Cluster();

        // Mock the Dao
        when(clusterDaoMock.getClustersWithPermittedAction(getUser().getId(), getActionGroup())).thenReturn(Collections.singletonList(expected));

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Cluster> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, actual.size(), "Wrong number of VDS Groups");
        assertEquals(expected, actual.get(0), "Wrong VDS Groups");
    }
}
