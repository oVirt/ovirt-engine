package org.ovirt.engine.extensions.aaa.builtin.tools;

import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_HELP;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LOG_FILE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LOG_LEVEL;

import org.ovirt.engine.core.utils.log.JavaLoggingUtils;

/**
 * Parses command line arguments, setups logging and executes engine-manage-domains
 */
public class ManageDomainsExecutor {
    public static void main(String... args) {
        ManageDomainsArguments mdArgs = null;
        try {
            mdArgs = new ManageDomainsArguments();
            mdArgs.parse(args);

            if (mdArgs.contains(ARG_LOG_FILE)) {
                JavaLoggingUtils.addFileHandler(mdArgs.get(ARG_LOG_FILE));
            }
            if (mdArgs.contains(ARG_LOG_LEVEL)) {
                JavaLoggingUtils.setLogLevel(mdArgs.get(ARG_LOG_LEVEL));
            }
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
