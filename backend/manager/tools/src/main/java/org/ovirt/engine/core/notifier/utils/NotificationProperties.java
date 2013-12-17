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
    public static final String ENGINE_PID = "ENGINE_PID";

    /**
     * This parameter specifies how many days of old events are processed and
     * sent when the notifier starts. If set to 2, for example, the notifier
     * will process and send the events of the last two days, older events will
     * just be marked as processed and won't be sent.
     */
    public static final String DAYS_TO_SEND_ON_STARTUP = "DAYS_TO_SEND_ON_STARTUP";

    /**
     * Comma separated list of recipients to be informed in case
     * the notification service cannot connect to the DB. can be empty.
     */
    public static final String FAILED_QUERIES_NOTIFICATION_RECIPIENTS = "FAILED_QUERIES_NOTIFICATION_RECIPIENTS";

    /**
     * Send a notification email after first failure to fetch notifications,
     * and then once every failedQueriesNotificationThreshold times.
     */
    public static final String FAILED_QUERIES_NOTIFICATION_THRESHOLD = "FAILED_QUERIES_NOTIFICATION_THRESHOLD";

    // Default files for defaults and overridden values:
    private static String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/notifier.conf.defaults";
    private static String VARS_PATH = "/etc/ovirt-engine/notifier/notifier.conf";

    // This is a singleton and this is the instance:
    private static NotificationProperties instance;

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

    /**
     * Validates properties values.
     *
     * @throws IllegalArgumentException if some properties has invalid values
     */
    public void validate() {
        // validate mandatory properties
        for (String property : new String[] {
                NotificationProperties.DAYS_TO_KEEP_HISTORY,
                NotificationProperties.ENGINE_INTERVAL_IN_SECONDS,
                NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS,
                NotificationProperties.INTERVAL_IN_SECONDS,
                NotificationProperties.IS_HTTPS_PROTOCOL,
                NotificationProperties.MAIL_PORT,
                NotificationProperties.MAIL_SERVER,
                NotificationProperties.REPEAT_NON_RESPONSIVE_NOTIFICATION }) {
            if (StringUtils.isEmpty(getProperty(property))) {
                throw new IllegalArgumentException(
                        String.format(
                                "Check configuration file, '%s' is missing",
                                property));
            }
        }

        if (!isSmtpEncryptionOptionValid()) {
            throw new IllegalArgumentException(
                    String.format(
                        "Check configuration file, '%s' value has to be one of: '%s', '%s', '%s'.",
                        NotificationProperties.MAIL_SMTP_ENCRYPTION,
                        NotificationProperties.MAIL_SMTP_ENCRYPTION_NONE,
                        NotificationProperties.MAIL_SMTP_ENCRYPTION_SSL,
                        NotificationProperties.MAIL_SMTP_ENCRYPTION_TLS
                    ));
        }

        // try to resolve MAIL_SERVER host
        try {
            InetAddress.getAllByName(getProperty(NotificationProperties.MAIL_SERVER));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format(
                            "Check configuration file, cannot verify '%s' value",
                            NotificationProperties.MAIL_SERVER),
                    ex);
        }

        // validate email addresses
        for (String property : new String[] {
                NotificationProperties.MAIL_USER,
                NotificationProperties.MAIL_FROM,
                NotificationProperties.MAIL_REPLY_TO }) {
            String candidate = getProperty(property);
            if (!StringUtils.isEmpty(candidate)) {
                try {
                    new InternetAddress(candidate);
                } catch(Exception ex) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Check configuration file, invalid format in '%s'",
                                    property),
                            ex);
                }
            }
        }

        // validate mail user value
        String emailUser = getProperty(NotificationProperties.MAIL_USER, true);
        if (StringUtils.isEmpty(emailUser)
                && (MAIL_SMTP_ENCRYPTION_SSL.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                        || MAIL_SMTP_ENCRYPTION_TLS.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                        || StringUtils.isNotEmpty(getProperty(NotificationProperties.MAIL_PASSWORD, true)))) {
                throw new IllegalArgumentException(
                        String.format(
                                "'%s' must be set when SSL or TLS is enabled or when password is set",
                                NotificationProperties.MAIL_USER));
        }
    }

    /**
     * Returns {@code true} if mail transport encryption type {@link MAIL_SMTP_ENCRYPTION} is correctly specified,
     * otherwise {@code false}
     */
    public boolean isSmtpEncryptionOptionValid() {
        return MAIL_SMTP_ENCRYPTION_NONE.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_SSL.equals(getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_TLS.equals(getProperty(MAIL_SMTP_ENCRYPTION, true));
    }
}
