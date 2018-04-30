package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.jgroups.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A test case for the {@link QueriesCommandBase} class. */
public class QueriesCommandBaseTest extends BaseCommandTest {
    private static final Logger log = LoggerFactory.getLogger(QueriesCommandBaseTest.class);

    @Mock
    private SessionDataContainer mockSessionDataContainer;

    @Mock
    private DbUser mockDbUser;

    @Mock
    private QueryParametersBase params;

    /* Getters and Setters tests */

    /** Test {@link QueriesCommandBase#isInternalExecution()} and {@link QueriesCommandBase#setInternalExecution(boolean)} */
    @Test
    public void testIsInternalExecutionDefault() {
        QueriesCommandBase<?> query = mockQuery();
        assertFalse(query.isInternalExecution(), "By default, a query should not be marked for internal execution");
    }

    @Test
    public void testIsInternalExecutionTrue() {
        QueriesCommandBase<?> query = mockQuery();
        query.setInternalExecution(true);
        assertTrue(query.isInternalExecution(), "Query should be marked for internal execution");
    }

    @Test
    public void testIsInternalExecutionFalse() {
        QueriesCommandBase<?> query = mockQuery();

        // Set as true, then override with false
        query.setInternalExecution(true);
        query.setInternalExecution(false);

        assertFalse(query.isInternalExecution(), "Query should not be marked for internal execution");
    }

    /** Test that an "oddly" typed query will be considered unknown */
    @Test
    public void testUnknownQuery() throws Exception {
        QueriesCommandBase<?> query = mockQuery();
        assertEquals(QueryType.Unknown, TestHelperQueriesCommandType.getQueryTypeFieldValue(query),
                "Wrong type for 'ThereIsNoSuchQuery'");
    }

    /** Tests Admin permission check */
    @Test
    public void testPermissionChecking() throws Exception {
        boolean[] booleans = { true, false };
        for (QueryType queryType : QueryType.values()) {
            for (boolean isFiltered : booleans) {
                for (boolean isUserAdmin : booleans) {
                    for (boolean isInternalExecution : booleans) {
                        boolean shouldBeAbleToRunQuery;
                        if (isFiltered) {
                            shouldBeAbleToRunQuery = !queryType.isAdmin();
                        } else {
                            shouldBeAbleToRunQuery = isInternalExecution || isUserAdmin;
                        }

                        log.debug("Running on query: {}", this);

                        String sessionId = getClass().getSimpleName();

                        // Mock parameters
                        when(params.isFiltered()).thenReturn(isFiltered);
                        when(params.getSessionId()).thenReturn(sessionId);

                        // Set up the user id env.
                        when(mockDbUser.isAdmin()).thenReturn(isUserAdmin);
                        when(mockSessionDataContainer.getUser(sessionId, false)).thenReturn(mockDbUser);

                        // Mock-Set the query as admin/user
                        QueriesCommandBase<?> query = mockQuery();
                        TestHelperQueriesCommandType.setQueryTypeFieldValue(query, queryType);

                        query.setInternalExecution(isInternalExecution);
                        query.executeCommand();
                        assertEquals(shouldBeAbleToRunQuery, query.getQueryReturnValue().getSucceeded(),
                                "Running with type=" + queryType + " isUserAdmin=" + isUserAdmin + " isFiltered="
                                        + isFiltered + " isInternalExecution=" + isInternalExecution + "\n " +
                                        "Query should succeed is: ");
                    }
                }
            }
        }
    }

    @Test
    public void testGetUserID() {
        when(mockDbUser.getId()).thenReturn(Guid.EVERYONE);
        String session = UUID.randomUUID().toString();
        when(mockSessionDataContainer.getUser(session, false)).thenReturn(mockDbUser);
        when(params.getSessionId()).thenReturn(session);
        when(params.getRefresh()).thenReturn(false);
        QueriesCommandBase<?> query = mockQuery();

        assertEquals(Guid.EVERYONE, query.getUserID(), "wrong guid");
    }

    @Test
    public void testGetUserIDWithNoUser() {
        QueriesCommandBase<?> query = mockQuery();

        assertNull(query.getUserID(), "wrong guid");
    }

    private QueriesCommandBase<?> mockQuery() {
        QueriesCommandBase<?> query = mock(QueriesCommandBase.class,
                withSettings().useConstructor(params, null).defaultAnswer(Answers.CALLS_REAL_METHODS));
        doReturn(mockSessionDataContainer).when(query).getSessionDataContainer();
        query.postConstruct();
        return query;
    }

    /* Test Utilities */

    @BeforeEach
    @AfterEach
    public void clearSession() {
        CorrelationIdTracker.clean();
    }
}
