package org.ovirt.engine.core.bll.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class SessionDataContainer {

    private ConcurrentMap<String, Map<String, Object>> oldContext =
            new ConcurrentHashMap<String, Map<String, Object>>();
    private ConcurrentMap<String, Map<String, Object>> newContext =
            new ConcurrentHashMap<String, Map<String, Object>>();

    private static final String VDC_USER_PARAMETER_NAME = "VdcUser";

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
    public final Object GetData(String key, boolean refresh) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId == null) {
            return null;
        }
        return GetData(sessionId, key, refresh);
    }

    /**
     * This method will set user by session which is attached to thread
     * @param key
     * @param value
     * @return At case when session is attached to thread will be return a true value
     */
    public final boolean SetData(String key, Object value) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (StringUtils.isEmpty(sessionId)) {
            return false;
        }
        SetData(sessionId, key, value);
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
    public final Object GetData(String sessionId, String key, boolean refresh) {
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

    public final void SetData(String sessionId, String key, Object value) {
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

    public boolean containsKey(String key) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId != null) {
            return oldContext.containsKey(key) || newContext.containsKey(key);
        }
        return false;
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
    public IVdcUser addUserToThreadContext(String sessionId, boolean refresh) {
        IVdcUser vdcUser = ThreadLocalParamsContainer.getVdcUser();
        if (vdcUser == null) {
            if (!StringUtils.isEmpty(sessionId)) {
                vdcUser = getUser(sessionId, refresh);
                ThreadLocalParamsContainer.setHttpSessionId(sessionId);
            } else {
                vdcUser = getUser(refresh);
            }
            ThreadLocalParamsContainer.setVdcUser(vdcUser);
        }
        return vdcUser;
    }

    /**
     * Sets the user for the given session Id
     * @param sessionId The session to set
     * @param user The user to set
     */
    public final void setUser(String sessionId, IVdcUser user) {
        SetData(sessionId, VDC_USER_PARAMETER_NAME, user);
    }

    /**
     * Sets the user for the current session
     * @param user The user to set
     */
    public final boolean setUser(IVdcUser user) {
        return SetData(VDC_USER_PARAMETER_NAME, user);
    }

    /**
     * @param sessionId The session to get the user for
     * @param refresh Whether refreshing the session is needed
     * @return The user set for the given {@link #session}
     */
    public IVdcUser getUser(String sessionId, boolean refresh) {
        return (IVdcUser) GetData(sessionId, VDC_USER_PARAMETER_NAME, refresh);
    }

    /** @return The user set in the current session */
    public IVdcUser getUser(boolean refresh) {
        return (IVdcUser) GetData(VDC_USER_PARAMETER_NAME, refresh);
    }
}
