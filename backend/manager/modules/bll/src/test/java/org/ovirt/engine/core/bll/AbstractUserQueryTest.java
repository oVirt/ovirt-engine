package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/** An abstract test class for query classes that handles common mocking requirements */
public abstract class AbstractUserQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>>
        extends AbstractQueryTest<P, Q> {

    protected static final long UNPRIVILEGED_USER_SESSION_ID = 1;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpMockQueryParameters();
    }

    /** Sets up a mock for {@link #params} */
    protected void setUpMockQueryParameters() {
        when(getQueryParameters().isFiltered()).thenReturn(true);
    }

    /** Verify that all queries tested in this manner were flagged as user queries in the {@link org.ovirt.engine.core.common.queries.VdcQueryType} enum */
    @Test
    public void testQueryIsAUserQuery() throws IllegalArgumentException, IllegalAccessException {
        assertFalse("A query tested for filtered access should not be an admin query",
                TestHelperQueriesCommandType.getQueryTypeFieldValue(getQuery()).isAdmin());
    }
}
