package org.ovirt.engine.core.utils.log;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Contains methods for runtime log4j setup
 */
public class Log4jUtils {
    /**
     * Resets logging configuration and tries to configure logging using specified configuration.
     *
     * @param url
     *            URL with log4j.xml
     */
    public static void setupLogging(URL url) {
        try {
            LogManager.resetConfiguration();
            DOMConfigurator.configure(url);
        } catch (FactoryConfigurationError ex) {
            throw new RuntimeException("Cannot configure logging: " + ex.getMessage(), ex);
        }
    }

    /**
     * Adds file appender with specified file and log level to root logger
     *
     * @param fileName
     *            file name to log into
     * @param levelName
     *            log level to use
     */
    public static void addFileAppender(String fileName, String levelName) {
        try {
            Level level = Level.INFO;
            if (levelName != null) {
                level = Level.toLevel(levelName, null);
                if (level == null) {
                    throw new IllegalArgumentException(String.format("Invalid log level value: '%s'", levelName));
                }
            }

            FileAppender fa = new FileAppender(new PatternLayout("%d %-5p [%c] %m%n"), fileName, true);
            fa.setThreshold(level);
            Logger.getRootLogger().addAppender(fa);
        } catch (SecurityException | IOException ex) {
            throw new IllegalArgumentException(
                    String.format("Error accessing log file '%s': '%s'", fileName, ex.getMessage()),
                    ex);
        }
    }
}
