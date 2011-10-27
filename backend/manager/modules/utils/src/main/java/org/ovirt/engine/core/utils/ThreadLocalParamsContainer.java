package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.interfaces.IVdcUser;

public class ThreadLocalParamsContainer {

    private static ThreadLocal<String> httpSessionId = new ThreadLocal<String>();
    private static ThreadLocal<IVdcUser> vdcUserKeeper = new ThreadLocal<IVdcUser>();

    public static void setHttpSessionId(String sessionId) {
        httpSessionId.set(sessionId);
    }

    public static String getHttpSessionId() {
        return httpSessionId.get();
    }

    public static void setVdcUser(IVdcUser vdcUser) {
        vdcUserKeeper.set(vdcUser);
    }

    public static IVdcUser getVdcUser() {
        return vdcUserKeeper.get();
    }

    public static void clean() {
        httpSessionId.remove();
        vdcUserKeeper.remove();
    }

}
