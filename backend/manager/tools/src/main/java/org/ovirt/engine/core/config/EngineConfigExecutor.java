package org.ovirt.engine.core.config;

import org.ovirt.engine.core.logutils.JavaLoggingUtils;
import org.slf4j.LoggerFactory;

/**
 * Parses command line arguments, setups logging and executes engine config
 */
public class EngineConfigExecutor {
    public static void main(String... args) {
        EngineConfigCLIParser parser = null;
        try {
            parser = new EngineConfigCLIParser();
            parser.parse(args);

            EngineConfigMap argsMap = parser.getEngineConfigMap();
            if (argsMap.getLogFile() != null) {
                JavaLoggingUtils.addFileHandler(argsMap.getLogFile());
            }
            if (argsMap.getLogLevel() != null) {
                JavaLoggingUtils.setLogLevel(argsMap.getLogLevel());
            }
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }

        try {
            EngineConfig.getInstance().setUpAndExecute(parser);
        } catch (Throwable t) {
            LoggerFactory.getLogger(EngineConfigExecutor.class).debug("Exiting with error: ", t);
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
}
