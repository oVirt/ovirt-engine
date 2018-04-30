package org.ovirt.engine.core.bll.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

/**
 * A test case for the {@link SessionDataContainer} class.
 */
@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionDataContainerTest {

    private static final String TEST_KEY = "someKey";
    private static final String TEST_VALUE = "someValue";
    private static final String TEST_SESSION_ID = "someSession";
    private static final String TEST_SSO_TOKEN = "someToken";
    private static final String USER = "user";
    private static final String SOFT_LIMIT = "soft_limit";

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.UserSessionTimeOutInterval, 30));
    }

    @Mock
    private EngineSessionDao engineSessionDao;

    @InjectMocks
    private SessionDataContainer container;

    @Mock
    private SessionDataContainer.SsoSessionValidator ssoSessionValidator;

    @Mock
    private SsoSessionUtils ssoSessionUtils;

    @BeforeEach
    public void setUpContainer() {
        when(engineSessionDao.remove(anyLong())).thenReturn(1);
        when(ssoSessionValidator.getSessionStatuses(any())).thenReturn(Collections.singletonMap(TEST_SSO_TOKEN, true));
        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(false);

        DbUser user = mock(DbUser.class);
        container.setUser(TEST_SESSION_ID, user);
        container.setSsoAccessToken(TEST_SESSION_ID, TEST_SSO_TOKEN);
    }

    public void clearSession() {
        container.removeSessionOnLogout(TEST_SESSION_ID);
    }

    @Test
    public void testGetDataAndSetDataWithEmptySession() {
        assertNull(container.getData("", TEST_KEY, false), "Get should return null with an empty session");
        clearSession();
    }

    @Test
    public void testGetDataAndSetDataWithFullSession() {
        container.setData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        assertEquals(TEST_VALUE, container.getData(TEST_SESSION_ID, TEST_KEY, false),
                "Get should return the value with a given session");
        clearSession();
    }

    @Test
    public void testGetUserAndSetUserWithSessionParam() {
        DbUser user = mock(DbUser.class);
        container.setUser(TEST_SESSION_ID, user);
        assertEquals(user, container.getUser(TEST_SESSION_ID, false),
                "Get should return the value with a given session");
        clearSession();
    }

    /* Tests for session management */

    @Test
    public void testRemoveWithParam() {
        // Set some data on the test sessions
        container.setData(TEST_SESSION_ID, TEST_KEY, TEST_VALUE);
        container.removeSessionOnLogout(TEST_SESSION_ID);
        assertNull(container.getData(TEST_SESSION_ID, TEST_KEY, false),
                "Get should return null since the session was removed");
    }
    /* Tests for clearedExpiredSessions */

    @Test
    public void testCleanExpiredSessions() {
        initDataForClearTest(TEST_KEY);
        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();
        assertNull(container.getData(TEST_SESSION_ID, TEST_KEY, false), "Get not find the session");
    }

    @Test
    public void testCleanExpiredSessionsWithRunningCommands() {
        when(ssoSessionUtils.isSessionInUse(anyLong())).thenReturn(true);

        initDataForClearTest(TEST_KEY);
        // Clear expired sessions - data is moved to older generation
        // nothing should happen as far as the user is concerned
        container.cleanExpiredUsersSessions();
        assertNotNull(container.getData(TEST_SESSION_ID, TEST_KEY, false), "Get found the session");
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
        assertNotNull(container.getData(TEST_SESSION_ID, USER, false),
                "Get should return null since the session wasn't refresh");
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
        assertNull(container.getData(TEST_SESSION_ID, USER, false),
                "Get should return null since the session wasn't refresh");
    }

}
