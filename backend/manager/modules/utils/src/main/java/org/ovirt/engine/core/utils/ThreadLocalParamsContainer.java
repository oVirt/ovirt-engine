package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.businessentities.DbUser;

public class ThreadLocalParamsContainer {

    private static ThreadLocal<String> httpSessionId = new ThreadLocal<>();
    private static ThreadLocal<DbUser> userKeeper = new ThreadLocal<>();

    /**
     * Identifies the correlation-id associated with the current thread
     */
    private static ThreadLocal<String> correlationId = new ThreadLocal<String>();

    public static void setHttpSessionId(String sessionId) {
        httpSessionId.set(sessionId);
    }

    public static String getHttpSessionId() {
        return httpSessionId.get();
    }

    public static void setUser(DbUser user) {
        userKeeper.set(user);
    }

    public static DbUser getUser() {
        return userKeeper.get();
    }

    /**
     * Set the value of the correlation-ID of the current thread and the value to be printed in the logger and past to
     * VDSM
     *
     * @param correlation
     *            The value of the correlation-ID to be logged
     */
    public static void setCorrelationId(String correlation) {
        correlationId.set(correlation);
    }

    public static String getCorrelationId() {
        return correlationId.get();
    }

    public static void clean() {
        httpSessionId.remove();
        userKeeper.remove();
        correlationId.remove();
    }

}
