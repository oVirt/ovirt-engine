package org.ovirt.engine.core.logutils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods for runtime java.util.logging setup
 */
public class JavaLoggingUtils {
    /**
     * Instance of org.ovirt logger. We need to keep instance of it to prevent OpenJDK incompatibility described at
     * http://findbugs.sourceforge.net/bugDescriptions.html#LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE
     */
    private static final Logger OVIRT_LOGGER = Logger.getLogger("org.ovirt");

    /**
     * Parses logging level from case insensitive string.
     *
     * @param levelName
     *            specified level name
     * @exception java.lang.IllegalArgumentException
     *                if unknown level is specified
     */
    public static Level parseLevel(String levelName) {
        if (levelName == null) {
            throw new IllegalArgumentException("Invalid log level value: 'null'");
        }
        return Level.parse(levelName.toUpperCase());
    }

    /**
     * Adds file handler with specified file to root logger
     *
     * @param fileName
     *            file name to log into
     */
    public static void addFileHandler(String fileName) {
        try {
            FileHandler fh = new FileHandler(fileName, true);
            fh.setFormatter(new TimeZoneBasedFormatter());
            Logger.getLogger("").addHandler(fh);
        } catch (SecurityException | IOException ex) {
            throw new IllegalArgumentException(
                    String.format("Error accessing log file '%s': '%s'", fileName, ex.getMessage()),
                    ex);
        }
    }

    /**
     * Sets level of org.ovirt logger
     *
     * @param levelName
     *            log level to use
     */
    public static void setLogLevel(String levelName) {
        OVIRT_LOGGER.setLevel(parseLevel(levelName));
    }
}
