package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.users.VdcUser;

public class ThreadLocalParamsContainer {

    private static ThreadLocal<String> httpSessionId = new ThreadLocal<String>();
    private static ThreadLocal<VdcUser> vdcUserKeeper = new ThreadLocal<VdcUser>();

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

    public static void setVdcUser(VdcUser vdcUser) {
        vdcUserKeeper.set(vdcUser);
    }

    public static VdcUser getVdcUser() {
        return vdcUserKeeper.get();
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
        vdcUserKeeper.remove();
        correlationId.remove();
    }

}
