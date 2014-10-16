package org.slf4j;

public class LoggerFactory {
    public static Logger getLogger(Class<?> loggedClass) {
        return new Logger(java.util.logging.Logger.getLogger(""));
    }
}
