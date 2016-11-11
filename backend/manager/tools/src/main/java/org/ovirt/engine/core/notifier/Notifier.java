package org.ovirt.engine.core.notifier;

import static org.ovirt.engine.core.notifier.utils.NotificationProperties.LOG_LEVEL;

import org.ovirt.engine.core.logutils.JavaLoggingUtils;
import org.ovirt.engine.core.notifier.transport.smtp.Smtp;
import org.ovirt.engine.core.notifier.transport.snmp.Snmp;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of event notification service. Initiate the service and handles termination signals
 */
public class Notifier {
    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    /**
     * Command line argument, that tells Notifier to validate properties only (it exits after validation)
     */
    private static final String ARG_VALIDATE = "validate";

    /**
     * @param args
     *            command line arguments, if {@code args[0]} contains {@code validate}, then only validation is
     *            executed and after that process ends. Otherwise process will continue to execute in standard way
     */
    public static void main(String[] args) {
        NotificationProperties prop = null;

        NotificationService notificationService = null;
        EngineMonitorService engineMonitorService = null;

        try {
            prop = NotificationProperties.getInstance();

            JavaLoggingUtils.setLogLevel(prop.getProperty(LOG_LEVEL));

            prop.validate();
            notificationService = new NotificationService(prop);
            engineMonitorService = new EngineMonitorService(prop);
            notificationService.registerTransport(new Smtp(prop));
            notificationService.registerTransport(new Snmp(prop));
            if (!notificationService.hasTransports()) {
                throw new RuntimeException(
                        "No transport is enabled, please enable at least one of SMTP (using MAIL_SERVER option)"
                        + " or SNMP (using SNMP_MANAGERS option) transports.");
            }
        } catch (Exception ex) {
            log.error("Failed to initialize", ex);
            // print error also to stderr to be seen in console during service startup
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        if (args != null && args.length > 0 && ARG_VALIDATE.equals(args[0])) {
            // command line argument to validate only entered
            System.exit(0);
        }

        try {
            notificationService.run();
            engineMonitorService.run();
        } catch (Exception e) {
            log.error("Failed to run the event notification service. ", e);
            // flag exit code to calling script after threads shut down.
            System.exit(1);
        }
    }
}

