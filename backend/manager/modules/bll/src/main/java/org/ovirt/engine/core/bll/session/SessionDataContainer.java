package org.ovirt.engine.core.bll.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class SessionDataContainer {

    private ConcurrentMap<String, Map<String, Object>> oldContext =
            new ConcurrentHashMap<String, Map<String, Object>>();
    private ConcurrentMap<String, Map<String, Object>> newContext =
            new ConcurrentHashMap<String, Map<String, Object>>();

    private static final String USER_PARAMETER_NAME = "user";
    private static final String PASSWORD_PARAMETER_NAME = "password";
    private static final String AUTHN_PARAMETER_NAME = "authn";
    private static final String PRINCIPAL_PARAMETER_NAME = "principal";

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
    public final Object getData(String key, boolean refresh) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId == null) {
            return null;
        }
        return getData(sessionId, key, refresh);
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
        Map<String, Object> currentContext = null;
        if ((currentContext = newContext.get(sessionId)) != null) {
            return currentContext.get(key);
        }
        if (refresh) {
            if ((currentContext = oldContext.remove(sessionId)) != null) {
                newContext.put(sessionId, currentContext);
            }
        } else {
            currentContext = oldContext.get(sessionId);
        }
        if (currentContext != null) {
            return currentContext.get(key);
        }
        return null;
    }

    public final void setData(String sessionId, String key, Object value) {
        // Try to get value from new generation
        Map<String, Object> context = newContext.get(sessionId);
        if (context == null) {
            // Try to get value from old generation
            context = oldContext.get(sessionId);
            if (context == null) {
                context = new ConcurrentHashMap<String, Object>();
            }
            // Put a value to new generation , for case that other thread put
            // value before current thread , his value will be used
            Map<String, Object> oldSessionContext = newContext.putIfAbsent(sessionId, context);
            if (oldSessionContext != null) {
                context = oldSessionContext;
            }
        }
        context.put(key, value);
    }

    /**
     * This method will move all newGeneration to old generation and old
     * generation will be cleaned automatically
     *
     * @return
     */
    private Map<String, Map<String, Object>> deleteOldGeneration() {
        Map<String, Map<String, Object>> temp = oldContext;
        oldContext = newContext;
        newContext = new ConcurrentHashMap<String, Map<String, Object>>();
        return temp;
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
        oldContext.remove(sessionId);
        newContext.remove(sessionId);
    }

    /**
     * Will run the process of cleaning expired sessions.
     */
    @OnTimerMethodAnnotation("cleanExpiredUsersSessions")
    public final void cleanExpiredUsersSessions() {
        deleteOldGeneration();
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
        return (DbUser) getData(USER_PARAMETER_NAME, refresh);
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
        return (String) getData(PASSWORD_PARAMETER_NAME, false);
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
}
