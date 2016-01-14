package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/** An abstract test class for query classes that handles common mocking requirements */
public abstract class AbstractUserQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>>
        extends AbstractQueryTest<P, Q> {

    protected static final long UNPRIVILEGED_USER_SESSION_ID = 1;

    private DbUser user;
    private Guid userID;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpMockUser();
        setUpMockQueryParameters();
        setUpSpyQuery();
    }

    /** Sets up a mock for {@link #user} */
    private void setUpMockUser() {
        userID = new Guid(UUID.randomUUID());
        user = mock(DbUser.class);
        when(user.getId()).thenReturn(userID);
    }

    /** Sets up a mock for {@link #params} */
    protected void setUpMockQueryParameters() {
        when(getQueryParameters().isFiltered()).thenReturn(true);
    }

    /** Sets up a mock for {@link #query} */
    @Override
    protected void setUpSpyQuery() throws Exception {
        super.setUpSpyQuery();
        when(getQuery().getUser()).thenReturn(user);
        when(getQuery().getUserID()).thenReturn(userID);
    }

    /** @return The mocked user to use in the test */
    protected DbUser getUser() {
        return user;
    }

    /** Verify that all queries tested in this manner were flagged as user queries in the {@link org.ovirt.engine.core.common.queries.VdcQueryType} enum */
    @Test
    public void testQueryIsAUserQuery() throws IllegalArgumentException, IllegalAccessException {
        assertFalse("A query tested for filtered access should not be an admin query",
                TestHelperQueriesCommandType.getQueryTypeFieldValue(getQuery()).isAdmin());
    }
}
