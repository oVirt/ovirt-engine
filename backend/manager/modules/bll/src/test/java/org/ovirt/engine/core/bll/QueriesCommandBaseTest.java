package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** A test case for the {@link QueriesCommandBase} class. */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MultiLevelAdministrationHandler.class)
public class QueriesCommandBaseTest {
    private static final Log log = LogFactory.getLog(QueriesCommandBaseTest.class);

    /* Getters and Setters tests */

    /** Test {@link QueriesCommandBase#isInternalExecution()} and {@link QueriesCommandBase#setInternalExecution(boolean) */
    @Test
    public void testIsInternalExecutionDefault() {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(mock(VdcQueryParametersBase.class));
        assertFalse("By default, a query should not be marked for internel execution", query.isInternalExecution());
    }

    @Test
    public void testIsInternalExecutionTrue() {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(mock(VdcQueryParametersBase.class));
        query.setInternalExecution(true);
        assertTrue("Query should be marked for internel execution", query.isInternalExecution());
    }

    @Test
    public void testIsInternalExecutionFalse() {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(mock(VdcQueryParametersBase.class));

        // Set as true, then override with false
        query.setInternalExecution(true);
        query.setInternalExecution(false);

        assertFalse("Query should not be marked for internel execution", query.isInternalExecution());
    }

    /** Test queries are created with the correct type */
    @SuppressWarnings("unchecked")
    @Test
    public void testQueryCreation() throws Exception {
        for (VdcQueryType queryType : VdcQueryType.values()) {
            try {
                log.debug("evaluating " + queryType);

                // Get the query's class
                Class<? extends QueriesCommandBase<?>> clazz =
                        (Class<? extends QueriesCommandBase<?>>) Class.forName(queryType.getPackageName() + "."
                                + queryType.name() + "Query");

                // Create a new instance, parameters don't matter
                Constructor<? extends QueriesCommandBase<?>> cons =
                        (Constructor<? extends QueriesCommandBase<?>>) clazz.getConstructors()[0];

                // Construct the parameter array
                Class<?>[] paramTypes = cons.getParameterTypes();
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; ++i) {
                    params[i] = mock(paramTypes[i]);
                }
                QueriesCommandBase<?> query = cons.newInstance(params);

                VdcQueryType type = TestHelperQueriesCommandType.getQueryTypeFieldValue(query);

                assertNotNull("could not find type", type);
                assertFalse("could not find type", type.equals(VdcQueryType.Unknown));
            } catch (ClassNotFoundException ignore) {
                log.debug("skipping");
            } catch (ExceptionInInitializerError ignore) {
                log.debug("skipping");
            }
        }
    }

    /** Test that an "oddly" typed query will be considered unknown */
    @Test
    public void testUnknownQuery() throws Exception {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(mock(VdcQueryParametersBase.class));
        assertEquals("Wrong type for 'ThereIsNoSuchQuery' ",
                VdcQueryType.Unknown,
                TestHelperQueriesCommandType.getQueryTypeFieldValue(query));
    }

    // TODO: Temporarily ignored until permission checking will be re-enabled, comment this back in when possible
    /** Tests Admin permission check */
    @Ignore
    @Test
    public void testPermissionChecking() throws Exception {
        boolean[] booleans = { true, false };
        for (VdcQueryType queryType : VdcQueryType.values()) {
            for (boolean isFiltered : booleans) {
                for (boolean isUserAdmin : booleans) {
                    for (boolean isInternalExecution : booleans) {
                        boolean shouldBeAbleToRunQuery =
                                isInternalExecution || isUserAdmin || (isFiltered && !queryType.isAdmin());

                        log.debug("Running on query: " + toString());

                        String sessionId = getClass().getSimpleName();

                        // Mock parameters
                        VdcQueryParametersBase params = mock(VdcQueryParametersBase.class);
                        when(params.isFiltered()).thenReturn(isFiltered);
                        when(params.getSessionId()).thenReturn(sessionId);

                        Guid guid = mock(Guid.class);

                        PowerMockito.mockStatic(MultiLevelAdministrationHandler.class);
                        when(MultiLevelAdministrationHandler.isAdminUser(guid)).thenReturn(isUserAdmin);

                        // Set up the user id env.
                        IVdcUser user = mock(IVdcUser.class);
                        when(user.getUserId()).thenReturn(guid);
                        ThreadLocalParamsContainer.setHttpSessionId(sessionId);
                        ThreadLocalParamsContainer.setVdcUser(user);

                        // Mock-Set the query as admin/user
                        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(params);
                        TestHelperQueriesCommandType.setQueryTypeFieldValue(query, queryType);

                        query.setInternalExecution(isInternalExecution);
                        query.ExecuteCommand();
                        assertEquals("Running with type=" + queryType + " isUserAdmin=" + isUserAdmin + " isFiltered="
                                + isFiltered + " isInternalExecution=" + isInternalExecution + "\n " +
                                "Query should succeed is: ", shouldBeAbleToRunQuery, query.getQueryReturnValue()
                                .getSucceeded());

                        ThreadLocalParamsContainer.clean();
                        SessionDataContainer.getInstance().removeSession();
                    }
                }
            }
        }
    }

    @Test
    public void testGetUserID() {
        IVdcUser user = mock(IVdcUser.class);
        when(user.getUserId()).thenReturn(Guid.EVERYONE);

        ThreadLocalParamsContainer.setVdcUser(user);
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(new VdcQueryParametersBase());

        assertEquals("wrong guid", Guid.EVERYONE, query.getUserID());
    }

    @Test
    public void testGetUserIDWithNoUser() {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(new VdcQueryParametersBase());

        assertEquals("wrong guid", null, query.getUserID());
    }

    /* Test Utilities */

    @Before
    @After
    public void clearSession() {
        ThreadLocalParamsContainer.clean();
        SessionDataContainer.getInstance().removeSession();
    }

    /** A stub class that will cause the {@link VdcQueryType#Unknown} to be used */
    private static class ThereIsNoSuchQuery extends QueriesCommandBase<VdcQueryParametersBase> {

        public ThereIsNoSuchQuery(VdcQueryParametersBase parameters) {
            super(parameters);
        }

        @Override
        protected void executeQueryCommand() {
            // Stub method, do nothing
        }
    }
}
