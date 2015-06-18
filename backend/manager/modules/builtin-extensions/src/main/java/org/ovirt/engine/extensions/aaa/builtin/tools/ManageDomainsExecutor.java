package org.ovirt.engine.extensions.aaa.builtin.tools;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.log.JavaLoggingUtils;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses command line arguments, setups logging and executes engine-manage-domains
 */
public class ManageDomainsExecutor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ManageDomainsExecutor.class);

    private static final String PROGRAM_NAME = System.getProperty("org.ovirt.engine.exttool.core.programName");
    private static final String PACKAGE_NAME = System.getProperty("org.ovirt.engine.exttool.core.packageName");
    private static final String PACKAGE_VERSION = System.getProperty("org.ovirt.engine.exttool.core.packageVersion");
    private static final String PACKAGE_DISPLAY_NAME = System.getProperty("org.ovirt.engine.exttool.core.packageDisplayName");
    private static final String ENGINE_ETC = System.getProperty("org.ovirt.engine.exttool.core.engineEtc");

    public static void main(String... args) {
        setupLogger();

        ArgumentsParser parser;
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(args));
        try {
            final Map<String, String> substitutions = new HashMap<>();
            substitutions.put("@ENGINE_ETC@", ENGINE_ETC);
            substitutions.put("@PROGRAM_NAME@", PROGRAM_NAME);

            try (InputStream stream = ManageDomainsExecutor.class.getResourceAsStream("arguments.properties")) {
                parser = new ArgumentsParser(stream, "module");
                parser.getSubstitutions().putAll(substitutions);
            }
            parser.parse(cmdArgs);
            Map<String, Object> argMap = parser.getParsedArgs();
            setupLogger(argMap);

            if (
                (Boolean)argMap.get("help") ||
                (cmdArgs.size() > 0 && cmdArgs.get(0).equals("help")) ||
                (cmdArgs.size() < 1)
            ) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ManageDomainsResult(ManageDomainsResultEnum.OK);
            } else if ((Boolean)argMap.get("version")) {
                System.out.format("%s-%s (%s)%n", PACKAGE_NAME, PACKAGE_VERSION, PACKAGE_DISPLAY_NAME);
                throw new ManageDomainsResult(ManageDomainsResultEnum.OK);
            }
            if (!parser.getErrors().isEmpty()) {
                for (Throwable t : parser.getErrors()) {
                    logger.error(t.getMessage());
                    logger.debug(t.getMessage(), t);
                }
                throw new ManageDomainsResult(
                    ManageDomainsResultEnum.ARGUMENT_PARSING_ERROR,
                    StringUtils.join(parser.getErrors(), ", ")
                );
            }
            String action = cmdArgs.remove(0);
            try (InputStream stream = ManageDomainsExecutor.class.getResourceAsStream("arguments.properties")) {
                parser = new ArgumentsParser(stream, action);
                parser.getSubstitutions().putAll(substitutions);
            }
            parser.parse(cmdArgs);
            argMap = parser.getParsedArgs();
            ManageDomains util = new ManageDomains(action, argMap);
            if(!util.isValidAction()) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, action);
            }
            if ((Boolean)argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ManageDomainsResult(ManageDomainsResultEnum.OK);
            }
            if(!parser.getErrors().isEmpty()) {
                for (Throwable t : parser.getErrors()) {
                    logger.error(t.getMessage());
                    logger.debug(t.getMessage(), t);
                }
                throw new ManageDomainsResult(
                    ManageDomainsResultEnum.ARGUMENT_PARSING_ERROR,
                    StringUtils.join(parser.getErrors(), ", ")
                );
            }
            // it's existence is checked during the parser validation
            util.init();
            util.createConfigurationProvider();
            util.runCommand();
            System.out.println(ManageDomainsResultEnum.OK.getDetailedMessage());
            System.exit(ManageDomainsResultEnum.OK.getExitCode());
        } catch (ManageDomainsResult e) {
            ManageDomains.exitOnError(e);
        } catch (Throwable t) {
            logger.error(t.getMessage());
            logger.debug(t.getMessage(), t);
            System.exit(1);
        }
    }

    private static void setupLogger() {
        String logLevel = System.getenv("OVIRT_LOGGING_LEVEL");
        JavaLoggingUtils.setLogLevel(logLevel != null ? logLevel : "INFO");
    }

    private static void setupLogger(Map<String, Object> args) {
        if (args.containsKey("log-file")) {
            JavaLoggingUtils.addFileHandler((String)args.get("log-file"));
        }
        if (args.containsKey("log-level")) {
            JavaLoggingUtils.setLogLevel((String)args.get("log-level"));
        }
    }
}
