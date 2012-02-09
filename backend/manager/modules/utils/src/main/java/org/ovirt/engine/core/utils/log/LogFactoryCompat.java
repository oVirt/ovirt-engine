package org.ovirt.engine.core.utils.log;

import org.apache.commons.logging.LogFactory;

public class LogFactoryCompat {

    public static LogCompat getLog(Class<?> loggedClass) {
        return new LogCompat(LogFactory.getLog(loggedClass));
    }
}
