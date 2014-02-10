package org.ovirt.engine.core.notifier.utils;

import java.net.InetAddress;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.LocalConfig;

/**
 * Defines properties uses by the event notification service
 */
public class NotificationProperties extends LocalConfig {

    /**
     * Service parameters
     */
    public static final String DAYS_TO_KEEP_HISTORY = "DAYS_TO_KEEP_HISTORY";
    public static final String INTERVAL_IN_SECONDS = "INTERVAL_IN_SECONDS";
    public static final String ENGINE_INTERVAL_IN_SECONDS = "ENGINE_INTERVAL_IN_SECONDS";
    public static final String ENGINE_TIMEOUT_IN_SECONDS = "ENGINE_TIMEOUT_IN_SECONDS";
    public static final String IS_HTTPS_PROTOCOL = "IS_HTTPS_PROTOCOL";
    public static final String SSL_PROTOCOL = "SSL_PROTOCOL";
    public static final String REPEAT_NON_RESPONSIVE_NOTIFICATION = "REPEAT_NON_RESPONSIVE_NOTIFICATION";
    public static final String ENGINE_MONITOR_RETRIES = "ENGINE_MONITOR_RETRIES";
    public static final String SSL_IGNORE_CERTIFICATE_ERRORS = "SSL_IGNORE_CERTIFICATE_ERRORS";
    public static final String SSL_IGNORE_HOST_VERIFICATION = "SSL_IGNORE_HOST_VERIFICATION";

    /**
     * Comma separated list of recipients to be informed in case the notification service cannot connect to the DB. can
     * be empty.
     */
    public static final String FAILED_QUERIES_NOTIFICATION_RECIPIENTS = "FAILED_QUERIES_NOTIFICATION_RECIPIENTS";

    /**
     * Send a notification email after first failure to fetch notifications, and then once every
     * failedQueriesNotificationThreshold times.
     */
    public static final String FAILED_QUERIES_NOTIFICATION_THRESHOLD = "FAILED_QUERIES_NOTIFICATION_THRESHOLD";

    /**
     * This parameter specifies how many days of old events are processed and sent when the notifier starts. If set to
     * 2, for example, the notifier will process and send the events of the last two days, older events will just be
     * marked as processed and won't be sent.
     */
    public static final String DAYS_TO_SEND_ON_STARTUP = "DAYS_TO_SEND_ON_STARTUP";

    /**
     * Email parameters
     */
    public static final String MAIL_SERVER = "MAIL_SERVER";
    public static final String MAIL_PORT = "MAIL_PORT";
    public static final String MAIL_USER = "MAIL_USER";
    public static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    public static final String MAIL_SMTP_ENCRYPTION = "MAIL_SMTP_ENCRYPTION";
    public static final String MAIL_FROM = "MAIL_FROM";
    public static final String MAIL_REPLY_TO = "MAIL_REPLY_TO";
    public static final String HTML_MESSAGE_FORMAT = "HTML_MESSAGE_FORMAT";

    /**
     * No SMTP transport encryption (plain SMTP)
     */
    public static final String MAIL_SMTP_ENCRYPTION_NONE = "none";

    /**
     * SMTP transport encryption using SSL (SMTPS)
     */
    public static final String MAIL_SMTP_ENCRYPTION_SSL = "ssl";

    /**
     * SMTP transport encryption using TLS (SMTP with STARTTLS)
     */
    public static final String MAIL_SMTP_ENCRYPTION_TLS = "tls";

    private static final String GENERIC_MESSAGE = "Check configuration file, ";

    // Default files for defaults and overridden values:
    private static String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/notifier.conf.defaults";
    private static String VARS_PATH = "/etc/ovirt-engine/notifier/notifier.conf";

    // This is a singleton and this is the instance:
    private static NotificationProperties instance;

    private NotificationProperties() {
        // Locate the defaults file and add it to the list:
        String defaultsPath = System.getenv("ENGINE_NOTIFIER_DEFAULTS");
        if (defaultsPath == null) {
            defaultsPath = DEFAULTS_PATH;
        }

        // Locate the overridden values file and add it to the list:
        String varsPath = System.getenv("ENGINE_NOTIFIER_VARS");
        if (varsPath == null) {
            varsPath = VARS_PATH;
        }

        loadConfig(defaultsPath, varsPath);
    }

    public static synchronized NotificationProperties getInstance() {
        if (instance == null) {
            instance = new NotificationProperties();
        }
        return instance;
    }

    public static void setDefaults(String defaultsPath, String varsPath) {
        DEFAULTS_PATH = defaultsPath;
        VARS_PATH = varsPath;
    }

    public static void release() {
        instance = null;
    }

    /**
     * Validates properties values.
     *
     * @throws IllegalArgumentException
     *             if some properties has invalid values
     */
    public void validate() {
        validateCommon();

        validateSmtp();
        if (isConfigured(MAIL_SERVER)) {
            validateSmtpAvailability();
        }
    }

    private void validateCommon() {
        requireAll(DAYS_TO_KEEP_HISTORY,
                DAYS_TO_SEND_ON_STARTUP,
                FAILED_QUERIES_NOTIFICATION_THRESHOLD,
                ENGINE_INTERVAL_IN_SECONDS,
                ENGINE_TIMEOUT_IN_SECONDS,
                INTERVAL_IN_SECONDS,
                IS_HTTPS_PROTOCOL,
                REPEAT_NON_RESPONSIVE_NOTIFICATION);
        // validate mandatory and non empty properties
        requireOne(MAIL_SERVER);

        // validate non negative args
        for (String property : new String[] {
                DAYS_TO_KEEP_HISTORY,
                DAYS_TO_SEND_ON_STARTUP,
                FAILED_QUERIES_NOTIFICATION_THRESHOLD }) {
            final String stringVal = getProperty(property);
            try {
                int value = Integer.parseInt(stringVal);
                if (value < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        String.format(
                                "'%s' must be a non negative integer.",
                                property));
            }
        }
    }

    private void validateSmtp() {
        // validate MAIL_PORT
        requireAll(MAIL_PORT);
        boolean mailPortValid = false;
        try {
            int port = new Integer(getProperty(MAIL_PORT));
            if (port > 0 && port < 65536) {
                mailPortValid = true;
            }
        } catch (NumberFormatException ex) {
        }
        if (!mailPortValid) {
            throw new IllegalArgumentException(
                    String.format("Check configuration file, MAIL_PORT value has to be in range from 1 to 65535,"
                            + " currently '%s'",
                            getProperty(MAIL_PORT)));
        }

        // validate MAIL_USER value
        String emailUser = getProperty(MAIL_USER, true);
        if (StringUtils.isEmpty(emailUser)
                && (MAIL_SMTP_ENCRYPTION_SSL.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                        || MAIL_SMTP_ENCRYPTION_TLS.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                        || StringUtils.isNotEmpty(getProperty(MAIL_PASSWORD, true)))) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be set when SSL or TLS is enabled or when password is set",
                            MAIL_USER));
        }

        if (!isSmtpEncryptionOptionValid()) {
            throw new IllegalArgumentException(
                    String.format(
                            GENERIC_MESSAGE + "'%s' value has to be one of: '%s', '%s', '%s'.",
                            MAIL_SMTP_ENCRYPTION,
                            MAIL_SMTP_ENCRYPTION_NONE,
                            MAIL_SMTP_ENCRYPTION_SSL,
                            MAIL_SMTP_ENCRYPTION_TLS
                            ));
        }

        // validate email addresses
        for (String property : new String[] {
                MAIL_USER,
                MAIL_FROM,
                MAIL_REPLY_TO }) {
            String candidate = getProperty(property);
            validateEmail(property, candidate);
        }
    }

    public boolean isConfigured(String property) {
        return !StringUtils.isEmpty(getProperty(property, true));
    }

    // Availability
    private void validateSmtpAvailability() {
        // try to resolve MAIL_SERVER host
        validateHost(MAIL_SERVER, getProperty(MAIL_SERVER));
    }

    private void validateHost(String propName, String propVal) {
        try {
            InetAddress.getAllByName(propVal);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format(
                            GENERIC_MESSAGE + "cannot verify '%s' value",
                            propName),
                    ex);
        }
    }

    private void requireAll(String... mandatoryProperties) {
        for (String property : mandatoryProperties) {
            if (StringUtils.isEmpty(getProperty(property, true))) {
                throw new IllegalArgumentException(
                        String.format(
                                GENERIC_MESSAGE + "'%s' is missing",
                                property));
            }
        }
    }

    private void requireOne(String... mandatoryProperties) {
        boolean provided = false;
        for (String property : mandatoryProperties) {
            if (isConfigured(property)) {
                provided = true;
            }
        }
        if (!provided) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(GENERIC_MESSAGE);
            String prefix = " ";
            for (String property : mandatoryProperties) {
                sb.append(prefix).append(property);
                prefix = " or ";
            }
            sb.append(" must be defined");
            throw new IllegalArgumentException(
                    sb.toString());
        }
    }

    private void validateEmail(String propName, String propVal) {
        if (!StringUtils.isEmpty(propVal)) {
            try {
                new InternetAddress(propVal);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        String.format(
                                GENERIC_MESSAGE + "invalid format in '%s'",
                                propName),
                        ex);
            }
        }
    }

    /**
     * Returns {@code true} if mail transport encryption type is correctly specified, otherwise {@code false}
     */
    public boolean isSmtpEncryptionOptionValid() {
        return MAIL_SMTP_ENCRYPTION_NONE.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_SSL.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_TLS.equals(getProperty(MAIL_SMTP_ENCRYPTION, true));
    }
}
