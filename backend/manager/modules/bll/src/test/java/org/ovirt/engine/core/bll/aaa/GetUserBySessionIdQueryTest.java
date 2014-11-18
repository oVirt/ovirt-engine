package org.ovirt.engine.core.bll.aaa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.EngineSessionDAO;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetUserBySessionIdQuery}.
 */
public class GetUserBySessionIdQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetUserBySessionIdQuery<VdcQueryParametersBase>> {

    private static final String sessionID = RandomUtils.instance().nextString(10);

    @Before
    public void setupEnvironment() {
        CorrelationIdTracker.clean();
        DbUser user = mock(DbUser.class);
        DbFacade dbFacadeMock = mock(DbFacade.class);
        SessionDataContainer.getInstance().setDbFacade(dbFacadeMock);

        EngineSessionDAO engineSessionDAOMock = mock(EngineSessionDAO.class);
        when(engineSessionDAOMock.remove(any(Long.class))).thenReturn(1);
        when(dbFacadeMock.getEngineSessionDao()).thenReturn(engineSessionDAOMock);

        SessionDataContainer.getInstance().setUser(sessionID, user);
    }

    @Test
    public void testExecuteQuery() {
        // Mock the SessionDataContainer
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);
        getQuery().executeQueryCommand();

        assertEquals("Wrong user returned from query", getUser(), getQuery().getQueryReturnValue().getReturnValue());
    }

    @After
    public void tearDown() {
        SessionDataContainer.getInstance().removeSessionOnLogout(sessionID);
    }
}
