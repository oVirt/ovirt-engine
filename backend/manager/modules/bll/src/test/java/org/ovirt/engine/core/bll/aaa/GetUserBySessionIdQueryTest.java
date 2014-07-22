package org.ovirt.engine.core.bll.aaa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetUserBySessionIdQuery}.
 */
public class GetUserBySessionIdQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetUserBySessionIdQuery<VdcQueryParametersBase>> {

    private static final String sessionID = RandomUtils.instance().nextString(10);

    @Test
    public void testExecuteQuery() {
        // Mock the SessionDataContainer
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);
        SessionDataContainer.getInstance().setUser(sessionID, getUser());

        getQuery().executeQueryCommand();

        assertEquals("Wrong user returned from query", getUser(), getQuery().getQueryReturnValue().getReturnValue());
    }

    @After
    public void tearDown() {
        SessionDataContainer.getInstance().removeSessionOnLogout(sessionID);
    }
}
