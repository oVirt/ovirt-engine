package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * A test case for {@link GetClustersWithPermittedActionQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetClustersWithPermittedActionQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetEntitiesWithPermittedActionParameters, GetClustersWithPermittedActionQuery<GetEntitiesWithPermittedActionParameters>> {

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        VDSGroup expected = new VDSGroup();

        // Mock the DAO
        VdsGroupDAO vdsGroupDAOMock = mock(VdsGroupDAO.class);
        when(vdsGroupDAOMock.getClustersWithPermittedAction(getUser().getId(), getActionGroup())).thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getVdsGroupDao()).thenReturn(vdsGroupDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VDSGroup> actual = (List<VDSGroup>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VDS Groups", 1, actual.size());
        assertEquals("Wrong VDS Groups", expected, actual.get(0));
    }
}
