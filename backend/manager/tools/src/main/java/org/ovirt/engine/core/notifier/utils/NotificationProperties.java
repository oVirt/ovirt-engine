package org.ovirt.engine.core.notifier.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.notifier.NotificationServiceException;
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
    public static final String MAIL_PORT_SSL = "MAIL_PORT_SSL";
    public static final String MAIL_USER = "MAIL_USER";
    public static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    public static final String MAIL_ENABLE_SSL = "MAIL_ENABLE_SSL";
    public static final String MAIL_FROM = "MAIL_FROM";
    public static final String MAIL_REPLY_TO = "MAIL_REPLY_TO";
    public static final String HTML_MESSAGE_FORMAT = "HTML_MESSAGE_FORMAT";

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

    private static final Logger log = Logger.getLogger(NotificationProperties.class);

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

}
