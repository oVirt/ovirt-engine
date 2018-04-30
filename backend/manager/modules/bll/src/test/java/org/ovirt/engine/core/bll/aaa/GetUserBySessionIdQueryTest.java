package org.ovirt.engine.core.bll.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetUserBySessionIdQuery}.
 */
public class GetUserBySessionIdQueryTest extends AbstractUserQueryTest<QueryParametersBase, GetUserBySessionIdQuery<QueryParametersBase>> {

    private static final String sessionID = RandomUtils.instance().nextString(10);

    @BeforeEach
    public void setupEnvironment() {
        CorrelationIdTracker.clean();
        DbUser user = mock(DbUser.class);

        when(engineSessionDao.remove(anyLong())).thenReturn(1);

        sessionDataContainer.setUser(sessionID, user);
    }

    @Test
    public void testExecuteQuery() {
        // Mock the SessionDataContainer
        when(getQueryParameters().getSessionId()).thenReturn(sessionID);
        getQuery().executeQueryCommand();

        assertEquals(getUser(), getQuery().getQueryReturnValue().getReturnValue(), "Wrong user returned from query");
    }

    @AfterEach
    public void tearDown() {
        sessionDataContainer.removeSessionOnLogout(sessionID);
    }
}
