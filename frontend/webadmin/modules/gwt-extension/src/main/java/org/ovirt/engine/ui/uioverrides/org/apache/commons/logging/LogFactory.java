package org.apache.commons.logging;

import java.util.logging.Logger;

public class LogFactory {
    public static Log getLog(Class<?> loggedClass) {
        return new Log(Logger.getLogger(""));
    }
}
