package org.ovirt.engine.core.notifier.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.notifier.NotificationServiceException;

/**
 * Defines properties uses by the event notification service
 */
public class NotificationProperties {
    /**
     * Database parameters
     */
    public static final String AS_DATA_SOURCE = "AS_DATA_SOURCE";
    public static final String AS_LOGIN_CONFIG = "AS_LOGIN_CONFIG";
    public static final String DB_CONNECTION_URL = "DB_CONNECTION_URL";
    public static final String DB_USER_NAME = "DB_USER_NAME";
    public static final String DB_PASSWORD = "DB_PASSWORD";
    public static final String DB_JDBC_DRIVER_CLASS = "DB_JDBC_DRIVER_CLASS";

    /**
     * Email parameters
     */
    public static final String MAIL_SERVER = "MAIL_SERVER";
    public static final String MAIL_PORT = "MAIL_PORT";
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
    public static final String ENGINE_ADDRESS = "ENGINE_ADDRESS"; // ip:port or hostname:port
    public static final String IS_HTTPS_PROTOCOL = "IS_HTTPS_PROTOCOL";
    public static final String SSL_PROTOCOL = "SSL_PROTOCOL";
    public static final String REPEAT_NON_RESPONSIVE_NOTIFICATION = "REPEAT_NON_RESPONSIVE_NOTIFICATION";
    public static final String ENGINE_MONITOR_RETRIES = "ENGINE_MONITOR_RETRIES";
    public static final String SSL_IGNORE_CERTIFICATE_ERRORS = "SSL_IGNORE_CERTIFICATE_ERRORS";
    public static final String SSL_IGNORE_HOST_VERIFICATION = "SSL_IGNORE_HOST_VERIFICATION";
    public static final String keystoreUrlVersion = "keystoreUrlVersion";
    public static final String keystorePassVersion = "keystorePassVersion";

    private static final LogCompat log = LogFactoryCompat.getLog(NotificationProperties.class);

    /**
     * Reads a properties file into a Map of <String,String> key-value pairs
     * @param propertiesFile
     *            the system dependent file name
     * @return a map which holds the properties from file
     * @throws NotificationServiceException
     *             exception for error reading the file
     */
    public static Map<String, String> readPropertiesFile(String propertiesFile) throws NotificationServiceException {
        FileInputStream inStream = null;
        Properties properties = new Properties();
        try {
            inStream = new FileInputStream(propertiesFile);
            properties.load(inStream);
        } catch (IOException e) {
            throw new NotificationServiceException("Failed to read configuration file " + propertiesFile, e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    throw new NotificationServiceException("Failed to close configuration file stream", e);
                }
            }
        }
        return fillPropertiesMap(properties);
    }

    /**
     * Populates properties collection, reporting on misconfigured properties. Is a duplicated property defined on
     * configuration file, warning is filed and the later property will be used.
     * @param properties
     *            properties which read from configuration file
     * @return a collections holds unique representation of the configuration properties
     */
    private static Map<String, String> fillPropertiesMap(Properties properties) {
        Map<String, String> prop = new HashMap<String, String>(properties.size());
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        String key;
        String value;
        for (Entry<Object, Object> entry : entrySet) {
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            if (prop.containsKey(key)) {
                log.error(String.format("Duplicate property [%s] is defined in configuration file. Using property with value [%s]",
                        key,
                        value));
            }
            prop.put(key, value);
        }
        return prop;
    }
}
