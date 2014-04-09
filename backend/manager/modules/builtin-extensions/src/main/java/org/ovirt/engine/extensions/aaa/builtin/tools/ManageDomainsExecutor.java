package org.ovirt.engine.extensions.aaa.builtin.tools;

import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_HELP;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LOG4J_CONFIG;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LOG_FILE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LOG_LEVEL;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.helpers.LogLog;
import org.ovirt.engine.core.utils.log.Log4jUtils;

/**
 * Parses command line arguments, setups logging and executes engine-manage-domains
 */
public class ManageDomainsExecutor {
    public static void setupLogging(String log4jConfig, String logFile, String logLevel) {
        URL cfgFileUrl = null;
        try {
            if (log4jConfig == null) {
                cfgFileUrl = ManageDomainsExecutor.class.getResource("/engine-manage-domains/log4j.xml");
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
        ManageDomainsArguments mdArgs = null;
        try {
            // suppress displaying log4j warnings due to accessing logs when parsing params
            LogLog.setQuietMode(true);
            mdArgs = new ManageDomainsArguments();
            mdArgs.parse(args);
            LogLog.setQuietMode(false);

            setupLogging(mdArgs.get(ARG_LOG4J_CONFIG), mdArgs.get(ARG_LOG_FILE), mdArgs.get(ARG_LOG_LEVEL));
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }

        try {
            if (mdArgs.contains(ARG_HELP)) {
                mdArgs.printHelp();
                System.exit(0);
            } else {
                ManageDomains util = new ManageDomains(mdArgs);
                // it's existence is checked during the parser validation
                util.init();
                util.createConfigurationProvider();
                util.runCommand();
            }
        } catch (ManageDomainsResult e) {
            ManageDomains.exitOnError(e);
        }
        System.out.println(ManageDomainsResultEnum.OK.getDetailedMessage());
        System.exit(ManageDomainsResultEnum.OK.getExitCode());
    }

}
