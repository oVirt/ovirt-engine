package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetUserBySessionIdQuery}.
 */
public class GetUserBySessionIdQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetUserBySessionIdQuery<VdcQueryParametersBase>> {

    @Test
    public void testExecuteQuery() {
        // Mock the SessionDataContainer
        String sessionID = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);
        SessionDataContainer.getInstance().SetData(sessionID, "VdcUser", getUser());

        getQuery().executeQueryCommand();

        assertEquals("Wrong user returned from query", getUser(), getQuery().getQueryReturnValue().getReturnValue());
    }
}
