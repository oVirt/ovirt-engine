package org.ovirt.engine.core.compat;

import java.util.logging.Logger;

public class LogFactoryCompat {
    public static LogCompat getLog(Class loggedClass) {
        return new LogCompat(Logger.getLogger(""));
    }
}
