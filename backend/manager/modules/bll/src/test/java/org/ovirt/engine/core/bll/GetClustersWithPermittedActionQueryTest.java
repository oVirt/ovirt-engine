package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetClustersWithPermittedActionQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetClustersWithPermittedActionQueryTest extends AbstractUserQueryTest<GetEntitiesWithPermittedActionParameters, GetClustersWithPermittedActionQuery<GetEntitiesWithPermittedActionParameters>> {

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        VDSGroup expected = new VDSGroup();

        // Set up the parameters
        ActionGroup actionGroup = RandomUtils.instance().pickRandom(ActionGroup.values());
        when(getQueryParameters().getActionGroup()).thenReturn(actionGroup);
        String sessionID = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);
        SessionDataContainer.getInstance().SetData(sessionID, "VdcUser", getUser());

        // Mock the DAO
        VdsGroupDAO vdsGroupDAOMock = mock(VdsGroupDAO.class);
        when(vdsGroupDAOMock.getClustersWithPermittedAction(getUser().getUserId(), actionGroup)).thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getVdsGroupDAO()).thenReturn(vdsGroupDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VDSGroup> actual = (List<VDSGroup>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VDS Groups", 1, actual.size());
        assertEquals("Wrong VDS Groups", expected, actual.get(0));
    }
}
