package org.ovirt.engine.core.bll.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

/**
 * A test case for the {@link SessionDataContainer} class.
 */
public class SessionDataContainerTest {

    private SessionDataContainer container;
    private static final String TEST_KEY = "someKey";
    private static final String TEST_VALUE = "someValue";
    private static final String TEST_SESSION_ID = "someSession";
    private static final String USER = "VdcUser";

    @Before
    public void setUpContainer() {
        container = spy(SessionDataContainer.getInstance());
        clearSession();
    }

    @After
    public void clearSession() {
        container.removeSession();
        container.removeSession(TEST_SESSION_ID);
        ThreadLocalParamsContainer.setHttpSessionId(null);
    }

    /* Tests for setData and getData */

    @Test
    public void testGetDataAndSetDataWithNullSession() {
        ThreadLocalParamsContainer.setHttpSessionId(null);
        assertFalse("Set should fail with a null session",
                container.SetData(TEST_KEY, TEST_VALUE));
        assertNull("Get should return null with a null session", container.GetData(TEST_KEY, false));
    }

    @Test
    public void testGetDataAndSetDataWithEmptySession() {
        ThreadLocalParamsContainer.setHttpSessionId("");
        assertFalse("Set should fail with an empty session",
                container.SetData(TEST_KEY, TEST_VALUE));
        assertNull("Get should return null with an empty session", container.GetData(TEST_KEY, false));
    }

    @Test
    public void testGetDataAndSetDataWithFullSession() {
        ThreadLocalParamsContainer.setHttpSessionId(TEST_SESSION_ID);
        assertTrue("Set should fail with an empty session",
                container.SetData(TEST_KEY, TEST_VALUE));
        assertEquals("Get should return null with an empty session",
                TEST_VALUE,
                container.GetData(TEST_KEY, false));
        assertEquals("Get should return the value with a given session",
                TEST_VALUE,
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testGetDataAndSetDataWithSessionParam() {
        container.SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        assertNull("Get should return null with an empty session", container.GetData(TEST_KEY, false));
        assertEquals("Get should return the value with a given session",
                TEST_VALUE,
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testGetUserAndSetUserWithSessionParam() {
        IVdcUser user = mock(IVdcUser.class);
        container.setUser(TEST_SESSION_ID, user);
        assertEquals("Get should return the value with a given session",
                user,
                container.getUser(TEST_SESSION_ID, false));
    }

    @Test
    public void testGetUserAndSetUserWithoutSessionParam() {
        ThreadLocalParamsContainer.setHttpSessionId(TEST_SESSION_ID);
        IVdcUser user = mock(IVdcUser.class);
        container.setUser(user);
        assertEquals("Get should return the value with a given session",
                user,
                container.getUser(false));
    }

    /* Tests for session management */

    @Test
    public void testRemoveWithParam() {
        // Set some data on the test sessions
        container.SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        container.removeSession(TEST_SESSION_ID);
        assertNull("Get should return null since the session was removed",
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testRemoveWithoutParam() {
        // Set some data on the test sessions
        container.SetData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);

        // Remove unset session and see it has no effect on the test session
        container.removeSession();
        assertEquals("Get should return the value since the session was not removed",
                TEST_VALUE,
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));

        // Remove the test session and see it has effect
        ThreadLocalParamsContainer.setHttpSessionId(TEST_SESSION_ID);
        container.removeSession();
        assertNull("Get should return the value since the session was removed",
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    /* Tests for clearedExpiredSessions */

    @Test
    public void testCleanNotExpiredUsersSessionsNoUsers() {
        initDataForClearTest(TEST_KEY);

        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();

        assertNotNull("Get should return the value since the session was not removed",
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testCleanExpiredUsersSessionsNoUsers() {
        initDataForClearTest(TEST_KEY);

        // Clear expired sessions twice - data is moved to older generation, then removed
        container.cleanExpiredUsersSessions();
        container.cleanExpiredUsersSessions();

        assertNull("Get should return null since the session was removed",
                container.GetData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testCleanNotExpiredUsersSessionsWithUsers() {
        initDataForClearTest(USER);

        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();

        assertNotNull("Get should return the value since the session was not removed",
                container.GetData(TEST_SESSION_ID, USER, false));
        assertNotNull("Get should return the value since the session was not removed",
                container.getUser(TEST_SESSION_ID, false));
    }

    @Test
    public void testCleanExpiredUsersSessionsWithUsers() {
        initDataForClearTest(USER);

        // Clear expired sessions twice - data is moved to older generation, then removed
        container.cleanExpiredUsersSessions();
        container.cleanExpiredUsersSessions();

        assertNull("Get should return null since the session was removed",
                container.GetData(TEST_SESSION_ID, USER, false));
        assertNull("Get should return null since the session was removed",
                container.getUser(TEST_SESSION_ID, false));
    }

    /** Initializes the {@link #key} data */
    private void initDataForClearTest(String key) {
        container.SetData(TEST_SESSION_ID, key, mock(IVdcUser.class));
    }

    @Test
    public void testRefreshUserSession() {
        initDataForClearTest(USER);

        // Clear expired sessions - data is moved to older generation
        container.cleanExpiredUsersSessions();

        // refresh the old session (refresh = true)
        container.GetData(TEST_SESSION_ID, USER, true);

        // cleared expired session
        container.cleanExpiredUsersSessions();

        // session should be already refreshed -> not null
        assertNotNull("Get should return null since the session wasn't refresh",
                container.GetData(TEST_SESSION_ID, USER, false));
    }

    @Test
    public void testRefreshUserSessionAfterExpiration() {
        initDataForClearTest(USER);

        // Clear expired sessions twice - data is moved to older generation, then removed
        container.cleanExpiredUsersSessions();
        container.cleanExpiredUsersSessions();

        // refresh the old session (refresh = true)
        // -> the user session is already expired so couldn't refresh it
        container.GetData(TEST_SESSION_ID, USER, true);

        // no session available
        assertNull("Get should return null since the session wasn't refresh",
                container.GetData(TEST_SESSION_ID, USER, false));
    }

}
