package org.ovirt.engine.core.bll.session;

import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class SessionDataContainer {

    private static class SessionInfo {
        private ConcurrentMap<String, Object> contentOfSession = new ConcurrentHashMap<>();

    }

    private ConcurrentMap<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    private static final String USER_PARAMETER_NAME = "user";
    private static final String PASSWORD_PARAMETER_NAME = "password";
    private static final String AUTHN_PARAMETER_NAME = "authn";
    private static final String PRINCIPAL_PARAMETER_NAME = "principal";
    private static final String HARD_LIMIT_PARAMETER_NAME = "hard_limit";
    private static final String SOFT_LIMIT_PARAMETER_NAME = "soft_limit";

    private static SessionDataContainer dataProviderInstance = new SessionDataContainer();

    private SessionDataContainer() {
    }

    public static SessionDataContainer getInstance() {
        return dataProviderInstance;
    }

    /**
     * Get data of session attached to current thread by key
     *
     * @param key
     *            - the internal key
     * @return
     */
    public final Object getData(String key) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId == null) {
            return null;
        }
        return getData(sessionId, key, false);
    }

    public final Object getData(String key, boolean refresh) {
        return getData(ThreadLocalParamsContainer.getHttpSessionId(), key, refresh);
    }


    /**
     * This method will set user by session which is attached to thread
     * @param key
     * @param value
     * @return At case when session is attached to thread will be return a true value
     */
    public final boolean setData(String key, Object value) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (StringUtils.isEmpty(sessionId)) {
            return false;
        }
        setData(sessionId, key, value);
        return true;
    }

    /**
     * Get data by session and internal key
     *
     * @param sessionId
     *            - id of session
     * @param key
     *            - the internal key
     * @param refresh
     *            - if perform refresh of session
     * @return
     */
    public final Object getData(String sessionId, String key, boolean refresh) {
        if (sessionId == null) {
            return null;
        }
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        Object value = null;
        if (sessionInfo != null) {
            if (refresh) {
                refresh(sessionInfo);

            }
            value = sessionInfo.contentOfSession.get(key);
        }
        return value;
    }

    public final void setData(String sessionId, String key, Object value) {
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        if (sessionInfo == null) {
            sessionInfo = new SessionInfo();
            SessionInfo oldSessionInfo = sessionInfoMap.putIfAbsent(sessionId, sessionInfo);
            if (oldSessionInfo != null) {
               sessionInfo = oldSessionInfo;
            }
         }
        sessionInfo.contentOfSession.put(key, value);
    }

    private SessionInfo getSessionInfo(String sessionId) {
        return sessionInfoMap.get(sessionId);
    }

    /**
     * Remove the cached data of session The sessionId is retrieved from
     * ThreadLocal
     */
    public final void removeSession() {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId != null) {
            removeSession(sessionId);
        }
    }

    /**
     * Remove the cached data of current session
     *
     * @param sessionId
     *            - id of current session
     */
    public final void removeSession(String sessionId) {
        sessionInfoMap.remove(sessionId);
    }

    /**
     * Will run the process of cleaning expired sessions.
     */
    @OnTimerMethodAnnotation("cleanExpiredUsersSessions")
    public final void cleanExpiredUsersSessions() {
        Date now = new Date();
        for (Entry<String, SessionInfo> entry : sessionInfoMap.entrySet()) {
            ConcurrentMap<String, Object> sessionMap = entry.getValue().contentOfSession;
            Date hardLimit = (Date) sessionMap.get(HARD_LIMIT_PARAMETER_NAME);
            Date softLimit = (Date) sessionMap.get(SOFT_LIMIT_PARAMETER_NAME);
            if ((hardLimit != null && hardLimit.before(now)) || (softLimit != null && softLimit.before(now))) {
                removeSessionImpl(entry.getKey());
            }
        }
    }
    /**
     * This method will add a user to thread local, at case that user is not
     * already added to context. If session is null or empty will try to get
     * session from thread local
     *
     * @param sessionId
     *            -id of session
     */
    public DbUser addUserToThreadContext(String sessionId, boolean refresh) {
        DbUser dbUser = ThreadLocalParamsContainer.getUser();
        if (dbUser == null) {
            if (!StringUtils.isEmpty(sessionId)) {
                dbUser = getUser(sessionId, refresh);
                ThreadLocalParamsContainer.setHttpSessionId(sessionId);
            } else {
                dbUser = getUser(refresh);
            }
            ThreadLocalParamsContainer.setUser(dbUser);
        }
        return dbUser;
    }

    /**
     * Sets the user for the given session Id
     * @param sessionId The session to set
     * @param user The user to set
     */
    public final void setUser(String sessionId, DbUser user) {
        setData(sessionId, USER_PARAMETER_NAME, user);
    }

    /**
     * Sets the user for the current session
     * @param user The user to set
     */
    public final boolean setUser(DbUser user) {
        return setData(USER_PARAMETER_NAME, user);
    }

    public final void setHardLimit(Date hardLimit) {
        setData(HARD_LIMIT_PARAMETER_NAME, hardLimit);
    }

    public final void setSoftLimit(Date softLimit) {
        setData(SOFT_LIMIT_PARAMETER_NAME, softLimit);
    }

    /**
     * @param sessionId The session to get the user for
     * @param refresh Whether refreshing the session is needed
     * @return The user set for the given {@link #session}
     */
    public DbUser getUser(String sessionId, boolean refresh) {
        return (DbUser) getData(sessionId, USER_PARAMETER_NAME, refresh);
    }

    /** @return The user set in the current session */
    public DbUser getUser(boolean refresh) {
        return (DbUser) getData(ThreadLocalParamsContainer.getHttpSessionId(), USER_PARAMETER_NAME, refresh);
    }

    /**
     * Sets the password of the user for the current session.
     *
     * @param user the password of the user
     */
    public void setPassword(String password) {
        setData(PASSWORD_PARAMETER_NAME, password);
    }

    /**
     * Returns the password of the current user stored in the session.
     *
     * @return an array of characters containing the password or
     *     <code>null</code> if the password is not available
     */
    public String getPassword(String sessionId) {
        return (String) getData(sessionId, PASSWORD_PARAMETER_NAME, false);
    }

    /**
     * Returns the password of the current user stored in the session.
     *
     * @return an array of characters containing the password or
     *     <code>null</code> if the password is not available
     */
    public String getPassword() {
        return (String) getData(PASSWORD_PARAMETER_NAME);
    }

    public void refresh() {
        refresh(getSessionInfo(ThreadLocalParamsContainer.getHttpSessionId()));
    }

    public ExtensionProxy getAuthn() {
        return (ExtensionProxy) getData(AUTHN_PARAMETER_NAME, false);
    }

    public void setAuthn(ExtensionProxy authn) {
        setData(AUTHN_PARAMETER_NAME, authn);
    }

    public void setPrincipal(String principal) {
        setData(PRINCIPAL_PARAMETER_NAME, principal);
    }

    public String getPrincipal() {
        return (String) getData(PRINCIPAL_PARAMETER_NAME, false);
    }
    private void refresh(SessionInfo sessionInfo) {
        int softLimitValue = Config.<Integer> getValue(ConfigValues.UserSessionTimeOutInterval);
        if (softLimitValue > 0) {
            sessionInfo.contentOfSession.put(SOFT_LIMIT_PARAMETER_NAME,
                    DateUtils.addMinutes(new Date(), softLimitValue));
        }
    }

    private void removeSessionImpl(String sessionId) {
        sessionInfoMap.remove(sessionId);
    }

}
