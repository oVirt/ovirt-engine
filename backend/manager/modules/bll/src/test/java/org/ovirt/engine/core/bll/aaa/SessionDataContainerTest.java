package org.ovirt.engine.core.bll.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.dao.EngineSessionDao;

/**
 * A test case for the {@link SessionDataContainer} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionDataContainerTest {

    private static final String TEST_KEY = "someKey";
    private static final String TEST_VALUE = "someValue";
    private static final String TEST_SESSION_ID = "someSession";
    private static final String USER = "user";
    private static final String SOFT_LIMIT = "soft_limit";

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.UserSessionTimeOutInterval, 2));

    @Mock
    private EngineSessionDao engineSessionDao;

    @InjectMocks
    private SessionDataContainer container;

    @Mock
    private SessionDataContainer.SsoSessionValidator ssoSessionValidator;

    @Mock
    private SsoSessionUtils ssoSessionUtils;

    @Before
    public void setUpContainer() {
        when(engineSessionDao.remove(any(Long.class))).thenReturn(1);
        when(ssoSessionValidator.isSessionValid(anyString())).thenReturn(true);
        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(false);

        DbUser user = mock(DbUser.class);
        container.setUser(TEST_SESSION_ID, user);
    }

    public void clearSession() {
        container.removeSessionOnLogout(TEST_SESSION_ID);
    }

    @Test
    public void testGetDataAndSetDataWithEmptySession() {
        assertNull("Get should return null with an empty session", container.getData("", TEST_KEY, false));
        clearSession();
    }

    @Test
    public void testGetDataAndSetDataWithFullSession() {
        container.setData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        assertEquals("Get should return the value with a given session",
                TEST_VALUE,
                container.getData(TEST_SESSION_ID, TEST_KEY, false));
        clearSession();
    }

    @Test
    public void testGetUserAndSetUserWithSessionParam() {
        DbUser user = mock(DbUser.class);
        container.setUser(TEST_SESSION_ID, user);
        assertEquals("Get should return the value with a given session",
                user,
                container.getUser(TEST_SESSION_ID, false));
        clearSession();
    }

    /* Tests for session management */

    @Test
    public void testRemoveWithParam() {
        // Set some data on the test sessions
        container.setData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        container.removeSessionOnLogout(TEST_SESSION_ID);
        assertNull("Get should return null since the session was removed",
                container.getData(TEST_SESSION_ID, TEST_KEY, false));
    }
    /* Tests for clearedExpiredSessions */

    @Test
    public void testCleanExpiredSessions() {
        initDataForClearTest(TEST_KEY);
        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();
        assertNull("Get not find the session",
                container.getData(TEST_SESSION_ID, TEST_KEY, false));
    }

    @Test
    public void testCleanExpiredSessionsWithRunningCommands() {
        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(true);

        initDataForClearTest(TEST_KEY);
        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();
        assertNotNull("Get found the session",
                container.getData(TEST_SESSION_ID, TEST_KEY, false));
    }

    /** Initializes the {@link #key} data */
    private void initDataForClearTest(String key) {
        container.setData(TEST_SESSION_ID, key, mock(DbUser.class));
        container.setData(TEST_SESSION_ID, SOFT_LIMIT, DateUtils.addMinutes(new Date(), -1));
    }

    @Test
    public void testRefreshUserSession() {
        // refresh the old session (refresh = true)
        container.getData(TEST_SESSION_ID, USER, true);

        // cleared expired session
        container.cleanExpiredUsersSessions();

        Object obj = container.getData(TEST_SESSION_ID, USER, false);

        // session should be already refreshed -> not null
        assertNotNull("Get should return null since the session wasn't refresh",
                container.getData(TEST_SESSION_ID, USER, false));
        clearSession();
    }

    @Test
    public void testRefreshUserSessionAfterExpiration() {
        initDataForClearTest(USER);

        // Clear expired sessions twice - data is moved to older generation, then removed
        container.cleanExpiredUsersSessions();
        container.cleanExpiredUsersSessions();

        // refresh the old session (refresh = true)
        // -> the user session is already expired so couldn't refresh it
        container.getData(TEST_SESSION_ID, USER, true);

        // no session available
        assertNull("Get should return null since the session wasn't refresh",
                container.getData(TEST_SESSION_ID, USER, false));
    }

}
