package org.ovirt.engine.core.notifier.utils;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.uutils.config.ShellLikeConfd;

/**
 * Defines properties uses by the event notification service
 */
public class NotificationProperties extends ShellLikeConfd {

    /**
     * Service parameters
     */
    public static final String LOG_LEVEL = "LOG_LEVEL";
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
     * Idle task interval
     * Interval in seconds to perform low priority tasks.
     */
    public static final String IDLE_INTERVAL = "IDLE_INTERVAL";

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
    }

    private void validateCommon() {
        requireAll(DAYS_TO_KEEP_HISTORY,
                DAYS_TO_SEND_ON_STARTUP,
                FAILED_QUERIES_NOTIFICATION_THRESHOLD,
                ENGINE_INTERVAL_IN_SECONDS,
                ENGINE_TIMEOUT_IN_SECONDS,
                INTERVAL_IN_SECONDS,
                IS_HTTPS_PROTOCOL,
                REPEAT_NON_RESPONSIVE_NOTIFICATION,
                IDLE_INTERVAL);

        // validate non negative args
        for (String property : new String[] {
                DAYS_TO_KEEP_HISTORY,
                DAYS_TO_SEND_ON_STARTUP,
                FAILED_QUERIES_NOTIFICATION_THRESHOLD,
                IDLE_INTERVAL }) {
            validateNonNegetive(property);
        }
    }

    public boolean isConfigured(String property) {
        return !StringUtils.isEmpty(getProperty(property, true));
    }

    public void requireAll(String... mandatoryProperties) {
        for (String property : mandatoryProperties) {
            if (StringUtils.isEmpty(getProperty(property, true))) {
                throw new IllegalArgumentException(
                        String.format(
                                GENERIC_MESSAGE + "'%s' is missing",
                                property));
            }
        }
    }

    public void requireOne(String... mandatoryProperties) {
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

    public int validateInteger(String property) {
        try {
            return Integer.parseInt(getProperty(property));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be an integer.",
                            property));
        }
    }

    public int validateNonNegetive(String property) {
        int value = validateInteger(property);
        if (value < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be a non negative integer.",
                            property));
        }
        return value;
    }

    public int validatePort(String property) {
        int value = validateInteger(property);
        if (value < 1 || value > 0xffff) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be a a valid port.",
                            property));
        }
        return value;
    }

    public InternetAddress validateEmail(String property) {
        try {
            InternetAddress ret = null;

            String value = getProperty(property);

            if (!StringUtils.isEmpty(value)) {
                ret = new InternetAddress(value);
            } else {
                ret = new InternetAddress();
            }

            return ret;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format(
                            GENERIC_MESSAGE + "invalid format in '%s'",
                            property),
                    ex);
        }
    }
}
