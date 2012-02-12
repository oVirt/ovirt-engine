package org.ovirt.engine.core.utils.log;

public class LogFactory {

    public static Log getLog(Class<?> loggedClass) {
        return new Log(org.apache.commons.logging.LogFactory.getLog(loggedClass));
    }
}
