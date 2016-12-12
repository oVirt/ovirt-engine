package org.slf4j;

/**
 * GWT override for SLF4J LoggerFactory.
 */
public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

}
