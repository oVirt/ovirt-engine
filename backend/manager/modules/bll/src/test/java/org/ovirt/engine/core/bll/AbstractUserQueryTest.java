package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

/** An abstract test class for query classes that handles common mocking requirements */
public abstract class AbstractUserQueryTest<P extends QueryParametersBase, Q extends QueriesCommandBase<? extends P>>
        extends AbstractQueryTest<P, Q> {

    protected static final long UNPRIVILEGED_USER_SESSION_ID = 1;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpMockQueryParameters();
    }

    /** Sets up a mock for {@link #params} */
    protected void setUpMockQueryParameters() {
        when(getQueryParameters().isFiltered()).thenReturn(true);
    }

    /** Verify that all queries tested in this manner were flagged as user queries in the {@link QueryType} enum */
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testQueryIsAUserQuery() throws IllegalArgumentException, IllegalAccessException {
        assertFalse(TestHelperQueriesCommandType.getQueryTypeFieldValue(getQuery()).isAdmin(),
                "A query tested for filtered access should not be an admin query");
    }
}
