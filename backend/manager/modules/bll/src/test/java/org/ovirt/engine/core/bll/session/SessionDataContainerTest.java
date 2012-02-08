package org.ovirt.engine.core.bll.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for the {@link SessionDataContainer} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DbFacade.class)
public class SessionDataContainerTest {

    private static final String TEST_KEY = "someKey";
    private static final String TEST_VALUE = "someValue";
    private static final String TEST_SESSION_ID = "someSession";

    @Before
    @After
    public void clearSession() {
        SessionDataContainer.getInstance().removeSession();
        SessionDataContainer.getInstance().removeSession(TEST_SESSION_ID);
        ThreadLocalParamsContainer.setHttpSessionId(null);
    }

    /* Tests for setData and getData */

    @Test
    public void testGetDataAndSetDataWithNullSession() {
        ThreadLocalParamsContainer.setHttpSessionId(null);
        assertFalse("Set should fail with a null session",
                SessionDataContainer.getInstance().SetData(TEST_KEY, TEST_VALUE));
        assertNull("Get should return null with a null session", SessionDataContainer.getInstance().GetData(TEST_KEY));
    }

    @Test
    public void testGetDataAndSetDataWithEmptySession() {
        ThreadLocalParamsContainer.setHttpSessionId("");
        assertFalse("Set should fail with an empty session",
                SessionDataContainer.getInstance().SetData(TEST_KEY, TEST_VALUE));
        assertNull("Get should return null with an empty session", SessionDataContainer.getInstance().GetData(TEST_KEY));
    }

    @Test
    public void testGetDataAndSetDataWithFullSession() {
        ThreadLocalParamsContainer.setHttpSessionId(TEST_SESSION_ID);
        assertTrue("Set should fail with an empty session",
                SessionDataContainer.getInstance().SetData(TEST_KEY, TEST_VALUE));
        assertEquals("Get should return null with an empty session",
                TEST_VALUE,
                SessionDataContainer.getInstance().GetData(TEST_KEY));
        assertEquals("Get should return the value with a given session",
                TEST_VALUE,
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
    }

    @Test
    public void testGetDataAndSetDataWithSessionParam() {
        SessionDataContainer.getInstance().SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        assertNull("Get should return null with an empty session", SessionDataContainer.getInstance().GetData(TEST_KEY));
        assertEquals("Get should return the value with a given session",
                TEST_VALUE,
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
    }

    /* Tests for session management */

    @Test
    public void testRemoveWithParam() {
        // Set some data on the test sessions
        SessionDataContainer.getInstance().SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        SessionDataContainer.getInstance().removeSession(TEST_SESSION_ID);
        assertNull("Get should return null since the session was removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
    }

    @Test
    public void testRemoveWithoutParam() {
        // Set some data on the test sessions
        SessionDataContainer.getInstance().SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);

        // Remove unset session and see it has no effect on the test session
        SessionDataContainer.getInstance().removeSession();
        assertEquals("Get should return the value since the session was not removed",
                TEST_VALUE,
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));

        // Remove the test session and see it has effect
        ThreadLocalParamsContainer.setHttpSessionId(TEST_SESSION_ID);
        SessionDataContainer.getInstance().removeSession();
        assertNull("Get should return the value since the session was removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
    }

    /* Tests for clearedExpiredSessions */

    @Test
    public void testCleanNotExpiredUsersSessionsNoUsers() {
        DbUserDAO dbUserDAOMcok = initDataForClearTest(TEST_KEY);

        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();

        assertNotNull("Get should return the value since the session was not removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
        verifyZeroInteractions(dbUserDAOMcok);
    }

    @Test
    public void testCleanExpiredUsersSessionsNoUsers() {
        DbUserDAO dbUserDAOMcok = initDataForClearTest(TEST_KEY);

        // Clear expired sessions twice - data is moved to older generation, then removed
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();

        assertNull("Get should return null since the session was removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, TEST_KEY));
        verifyZeroInteractions(dbUserDAOMcok);
    }

    @Test
    public void testCleanNotExpiredUsersSessionsWithUsers() {
        DbUserDAO dbUserDAOMcok = initDataForClearTest("VdcUser");

        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();

        assertNotNull("Get should return the value since the session was not removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, "VdcUser"));
        assertNotNull("Get should return the value since the session was not removed",
                SessionDataContainer.getInstance().getUser(TEST_SESSION_ID));
        verifyZeroInteractions(dbUserDAOMcok);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCleanExpiredUsersSessionsWithUsers() {
        DbUserDAO dbUserDAOMcok = initDataForClearTest("VdcUser");

        // Clear expired sessions twice - data is moved to older generation, then removed
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();
        SessionDataContainer.getInstance().cleanExpiredUsersSessions();

        assertNull("Get should return null since the session was removed",
                SessionDataContainer.getInstance().GetData(TEST_SESSION_ID, "VdcUser"));
        assertNull("Get should return null since the session was removed",
                SessionDataContainer.getInstance().getUser(TEST_SESSION_ID));
        verify(dbUserDAOMcok).removeUserSessions(anyMap());
    }

    private static DbUserDAO initDataForClearTest(String key) {
        DbUserDAO dbUserDAOMcok = mock(DbUserDAO.class);

        DbFacade dbFacadeMock = mock(DbFacade.class);
        when(dbFacadeMock.getDbUserDAO()).thenReturn(dbUserDAOMcok);

        PowerMockito.mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacadeMock);

        // Set some data
        SessionDataContainer.getInstance().SetData(TEST_SESSION_ID, key, mock(VdcUser.class));
        return dbUserDAOMcok;
    }

}
