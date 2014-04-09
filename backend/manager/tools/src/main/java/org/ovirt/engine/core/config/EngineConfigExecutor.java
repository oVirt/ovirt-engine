package org.ovirt.engine.core.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.log.Log4jUtils;

/**
 * Parses command line arguments, setups logging and executes engine config
 */
public class EngineConfigExecutor {
    public static void setupLogging(String log4jConfig, String logFile, String logLevel) {
        URL cfgFileUrl = null;
        try {
            if (log4jConfig == null) {
                cfgFileUrl = EngineConfigExecutor.class.getResource("/engine-config/log4j.xml");
            } else {
                cfgFileUrl = new File(log4jConfig).toURI().toURL();
            }
            Log4jUtils.setupLogging(cfgFileUrl);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    String.format("Error loading log4j configuration from '%s': %s", cfgFileUrl, ex.getMessage()),
                    ex);
        }

        if (logFile != null) {
            Log4jUtils.addFileAppender(logFile, logLevel);
        }
    }

    public static void main(String... args) {
        EngineConfigCLIParser parser = null;
        try {
            parser = new EngineConfigCLIParser();
            parser.parse(args);

            EngineConfigMap argsMap = parser.getEngineConfigMap();
            setupLogging(argsMap.getLog4jConfig(), argsMap.getLogFile(), argsMap.getLogLevel());
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }

        try {
            EngineConfig.getInstance().setUpAndExecute(parser);
        } catch (Throwable t) {
            Logger.getLogger(EngineConfigExecutor.class).debug("Exiting with error: ", t);
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
}
